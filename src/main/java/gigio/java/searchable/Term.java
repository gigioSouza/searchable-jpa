package gigio.java.searchable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Term {
	private String term;
	private List<String> listTerm;
	private String operator;
	private String value;
	private List<String> listValue;

	public Term(String term, String operator, Object value) {
		this.term = term;
		this.operator = operator;
		this.value = value.toString();
	}

	public Term(String term, String operator, List<Object> listValue) {
		this.term = term;
		this.operator = operator;
		this.listValue = listValue.stream().map(Object::toString).collect(Collectors.toList());
	}

	public Term(String term, String operator, Object... listValue) {
		this.term = term;
		this.operator = operator;
		this.listValue = Arrays.asList(listValue).stream().map(Object::toString).collect(Collectors.toList());
	}
	
	public Term(List<String> term, String operator, Object value) {
		this.listTerm = term;
		this.operator = operator;
		this.value = value.toString();
	}

	public Term(List<String> term, String operator, List<Object> listValue) {
		this.listTerm = term;
		this.operator = operator;
		this.listValue = listValue.stream().map(Object::toString).collect(Collectors.toList());
	}

	public Term(List<String> term, String operator, Object... listValue) {
		this.listTerm = term;
		this.operator = operator;
		this.listValue = Arrays.asList(listValue).stream().map(Object::toString).collect(Collectors.toList());
	}
}
