/*
 * Created on 20.10.2005
 */
package ch0p.datastrutures.parsegraph;

import java.util.ArrayList;
import java.util.List;

import ch0p.datastrutures.Symbol;

/**
 * @author Tom Gelhausen
 */
public class Occurrence {
	private static int IDcounter = 0;
	private final int hv;
	private List<Application> readers = new ArrayList<Application>();
	private Application source;
	private final Symbol symbol;

	public Occurrence(Symbol s) {
		symbol = s;
		hv = (int) (symbol.hashCode() ^ IDcounter++ ^ System.currentTimeMillis());
	}

	@Override
	public final boolean equals(Object o) {
		if (o == this) return true;
		return false;
	}

	public final List<Application> getReaders() {
		return readers;
	}

	public final Application getSource() {
		return source;
	}

	public final Symbol getSymbol() {
		return symbol;
	}

	@Override
	public final int hashCode() {
		return hv;
	}

	@Override
	public final String toString() {
		return symbol.toString();
	}

	protected void addReader(Application reader) {
		readers.add(reader);
	}

	protected void setSource(Application source) {
		this.source = source;
	}
}
