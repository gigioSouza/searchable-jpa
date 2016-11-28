package gigio.java.searchable;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Searchable<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Term> terms;
	private List<Ordering> ordering;
	private int page;
	private int size;
	private boolean normalize;

	public PageRequest getPageable() {
		Sort sort = null;

		if (ordering != null) {
			List<Order> orders = new ArrayList<Order>();
			for (Ordering ob : ordering) {
				orders.add(new Order(
						ob.getTerm().toUpperCase().equals(Ordering.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC,
						ob.getTerm()));
			}
			sort = new Sort(orders);
		}

		return new PageRequest(page, size, sort);
	}

	@SuppressWarnings({ "unchecked", "hiding", "rawtypes" })
	public <T> Specification<T> getSpecification() {
		return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
			Predicate predicate = builder.conjunction();
			if (terms != null) {
				for (Term searchTerm : terms) {
					String campo = searchTerm.getTerm();
					if (campo != null && !campo.isEmpty()) {
						String prop = campo;
						String operator = searchTerm.getOperator();
						String value = searchTerm.getValue();
						value = value == null ? null : value.trim();
						List<String> valueList = searchTerm.getListValue() == null ? null : searchTerm.getListValue();
						Predicate predicateTerm = null;
						Expression<String> col;
						if (prop.contains(".")) {
							String[] split = prop.split("\\.");
							Join lastJoin = null;
							for (int i = 0; i < split.length - 1; i++) {
								if (lastJoin == null) {
									lastJoin = root.join(split[i]);
								} else {
									lastJoin = lastJoin.join(split[i]);
								}
								lastJoin.alias(split[i]);
							}
							col = lastJoin.get(split[split.length - 1]).as(String.class);
						} else {
							col = root.get(prop).as(String.class);
						}

						if(normalize) value = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
						
						switch (operator) {
						case "like":
							predicateTerm = builder.like(builder.lower(col), "%" + value.toLowerCase() + "%");
							break;
						case "likeBegin":
							predicateTerm = builder.like(builder.lower(col), "%" + value.toLowerCase());
							break;
						case "likeEnd":
							predicateTerm = builder.like(builder.lower(col), value.toLowerCase() + "%");
							break;
						case "gt":
							predicateTerm = builder.greaterThan(col, value);
							break;
						case "gte":
							predicateTerm = builder.greaterThanOrEqualTo(col, value);
							break;
						case "eq":
							predicateTerm = builder.equal(builder.lower(col), value.toLowerCase());
							break;
						case "lte":
							predicateTerm = builder.lessThanOrEqualTo(col, value);
							break;
						case "lt":
							predicateTerm = builder.lessThan(col, value);
							break;
						case "isnull":
							predicateTerm = builder.isNull(col);
							break;
						case "in":
							predicateTerm = col.in(valueList);
							break;
						default:
							continue;
						}
						predicate.getExpressions().add(builder.and(predicateTerm));
					}
				}
			}
			return predicate;
		};
	}
}
