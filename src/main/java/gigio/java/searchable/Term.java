package gigio.java.searchable;

import java.util.Arrays;
import java.util.List;

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

	public Term(String term, String operator, String value) {
		this.term = term;
		this.operator = operator;
		this.value = value;
	}

	public Term(String term, String operator, List<String> listValue) {
		this.term = term;
		this.operator = operator;
		this.listValue = listValue;
	}

	public Term(String term, String operator, String... listValue) {
		this.term = term;
		this.operator = operator;
		this.listValue = Arrays.asList(listValue);
	}
	
	public Term(List<String> term, String operator, String value) {
		this.listTerm = term;
		this.operator = operator;
		this.value = value;
	}

	public Term(List<String> term, String operator, List<String> listValue) {
		this.listTerm = term;
		this.operator = operator;
		this.listValue = listValue;
	}

	public Term(List<String> term, String operator, String... listValue) {
		this.listTerm = term;
		this.operator = operator;
		this.listValue = Arrays.asList(listValue);
	}
}
