package ch0p.datastrutures;

/**
 * @author Tom Gelhausen
 */
public class Terminal extends Symbol {
	
	private String stringRepresentation;
	
	public Terminal(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation.intern();
	}

	@Override
	public void append(StringBuilder sb) {
		sb.append(stringRepresentation);		
	}

	@Override
	public boolean matches(Symbol s) {
		if (!(s instanceof Terminal)) return false;
		Terminal t = (Terminal) s;
		return stringRepresentation==t.stringRepresentation;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Terminal)) return false;
		Terminal t = (Terminal) o;
		return stringRepresentation==t.stringRepresentation;
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
		return stringRepresentation.hashCode() ^ 17;
	}

	public int compareTo(Symbol that) {
		if (this==that) return 0;
		String str = that.getStringRepresentation();
		return stringRepresentation.compareTo(str);
	}

}
