package gigio.java.searchable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Ordering {
	public final static String ASC = "ASC";
	public final static String DESC = "DESC";

	private String term;
	private String direction;

	public Ordering(String campo) {
		this.term = campo;
		this.direction = ASC;
	}

	public Ordering(String campo, String ordem) {
		this.term = campo;
		if (!ordem.toUpperCase().equals(ASC) && !ordem.toUpperCase().equals(DESC))
			throw new NullPointerException("Invalid ordering direction");
		this.direction = ordem;
	}
}
