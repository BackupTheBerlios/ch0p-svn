package ch0p.datastrutures;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Gelhausen
 */
public abstract class Symbol implements Comparable<Symbol> {
	private List<Symbol> buffer;
	
	public abstract void append(StringBuilder sb);
	public abstract boolean matches(Symbol s);
	public abstract String getStringRepresentation();
	
	public List<Symbol> asList() {
		if (buffer==null) {
			buffer = new ArrayList<Symbol>(1);
			buffer.add(0, this);
		}
		return buffer;
	}
}
