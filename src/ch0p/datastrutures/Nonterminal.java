package ch0p.datastrutures;

/**
 * @author Tom Gelhausen
 */
public class Nonterminal extends Symbol {

	private String stringRepresentation;
	
	public Nonterminal(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation.intern();
	}

	@Override
	public void append(StringBuilder sb) {
		sb.append(stringRepresentation);		
	}

	@Override
	public boolean matches(Symbol s) {
		if (!(s instanceof Nonterminal)) return false;
		Nonterminal nt = (Nonterminal) s;
		return (stringRepresentation==nt.stringRepresentation);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Nonterminal)) return false;
		Nonterminal nt = (Nonterminal) o;
		return (stringRepresentation==nt.stringRepresentation);
	}
	
	@Override
	public String toString() {
		return stringRepresentation;
	}

	/**
	 * @return Returns the stringRepresentation.
	 */
	@Override
	public String getStringRepresentation() {
		return stringRepresentation;
	}

	@Override
	public int hashCode() {
		return stringRepresentation.hashCode() ^ 19;
	}

	public int compareTo(Symbol that) {
		if (this==that) return 0;
		String str = that.getStringRepresentation();
		return stringRepresentation.compareTo(str);
	}

}
