/*
 * Created on 04.11.2005
 */
package ch0p.datastrutures;

/**
 * @author Tom Gelhausen
 */
public class Epsilon extends Symbol {
	private static final char c_epsilon = '\u03B5'; 
	private static final String s_epsilon = "\u03B5"; 
	
	public static final Epsilon instance = new Epsilon();
	
	private Epsilon() {
	}

	@Override
	public void append(StringBuilder sb) {
		sb.append(c_epsilon);
	}

	@Override
	public boolean matches(@SuppressWarnings("unused") Symbol s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getStringRepresentation() {
		return s_epsilon;
	}

	public int compareTo(Symbol o) {
		if (o==this) return 0;
		return -1;
	}

}
