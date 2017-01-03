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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Searchable<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Singular
	private List<Term> terms;
	@Singular
	private List<Ordering> orderings;
	private int pageNumber;
	private int pageSize;
	private boolean normalize;
	
	public Searchable(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
	}

	public PageRequest getPageable() {
		Sort sort = null;

		if (orderings != null) {
			List<Order> orders = new ArrayList<Order>();
			for (Ordering ob : orderings) {
				orders.add(new Order(
						ob.getDirection().toUpperCase().equals(Ordering.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC,
						ob.getTerm()));
			}
			sort = new Sort(orders);
		}

		return new PageRequest(pageNumber, pageSize, sort);
	}

	@SuppressWarnings({ "hiding" })
	public <T> Specification<T> getSpecification() {
		return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
			Predicate conjunction = builder.conjunction();
			if (terms != null) {
				for (Term searchTerm : terms) {
					String operator = searchTerm.getOperator();
					String value = searchTerm.getValue();
					value = value == null ? null : value.trim();
					List<String> listValue = searchTerm.getListValue() == null ? null : searchTerm.getListValue();
					
					if(normalize) value = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");

					if(searchTerm.getTerm() != null) {
						String field = searchTerm.getTerm();
						if (field != null && !field.isEmpty()) {
							Expression<String> property = resolveColumn(root, field);
							Predicate predicateTerm = resolvePredicate(builder, property, operator, value, listValue);							
							conjunction.getExpressions().add(builder.and(predicateTerm));
						}						
					} else if(searchTerm.getListTerm() != null) {
						Predicate disjunction = builder.disjunction();
						for(String field : searchTerm.getListTerm()) {
							Expression<String> property = resolveColumn(root, field);
							Predicate predicateTerm = resolvePredicate(builder, property, operator, value, listValue);							
							disjunction.getExpressions().add(builder.and(predicateTerm));
						}
						conjunction.getExpressions().add(builder.and(disjunction));
					}
				}
			}
			return conjunction;
		};
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Expression<String> resolveColumn(Root root, String property){
		String prop = property;
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
		return col;
	}
	
	private Predicate resolvePredicate(CriteriaBuilder builder, Expression<String> property, String operator, String value, List<String> listValue) {
		Predicate predicate = null;
		switch (operator) {
		case "like":
			predicate = builder.like(builder.lower(property), "%" + value.toLowerCase() + "%");
			break;
		case "likeBegin":
			predicate = builder.like(builder.lower(property), "%" + value.toLowerCase());
			break;
		case "likeEnd":
			predicate = builder.like(builder.lower(property), value.toLowerCase() + "%");
			break;
		case "gt":
			predicate = builder.greaterThan(property, value);
			break;
		case "gte":
			predicate = builder.greaterThanOrEqualTo(property, value);
			break;
		case "eq":
			predicate = builder.equal(builder.lower(property), value.toLowerCase());
			break;
		case "lte":
			predicate = builder.lessThanOrEqualTo(property, value);
			break;
		case "lt":
			predicate = builder.lessThan(property, value);
			break;
		case "isnull":
			predicate = builder.isNull(property);
			break;
		case "isnotnull":
			predicate = builder.isNotNull(property);
			break;
		case "in":
			predicate = property.in(listValue);
			break;
		}
		return predicate;
	}
}
