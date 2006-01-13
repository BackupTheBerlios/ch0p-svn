/*
 * Created on 04.10.2005
 */
package ch0p.datastrutures;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Tom Gelhausen
 */
public class SymbolSequence implements Iterable<Symbol>, Comparable<SymbolSequence> {
	private List<Symbol> data_incl = new ArrayList<Symbol>();
	private List<Symbol> data_excl = new ArrayList<Symbol>();
	private int hashcode = 0;
	private boolean containsNT = false;

	public void append(Symbol s) {
		data_incl.add(s);
		if (!(s instanceof Epsilon)) {
			data_excl.add(s);
		}
		hashcode = 0;
		if (s instanceof Nonterminal) containsNT = true;
	}

	public void append(SymbolSequence ss) {
		data_incl.addAll(ss.data_incl);
		data_excl.addAll(ss.data_excl);
		hashcode = 0;
		for (Symbol s : ss.data_excl) {
			if (s instanceof Nonterminal) {
				containsNT = true;
				break;
			}
		}
	}

	public boolean containsNonterminals() {
		return containsNT;
	}

	/**
	 * does the same as a call of <code>appendTo(sb, false)</code> 
	 */
	public void appendTo(StringBuilder sb) {
		appendTo(sb, false);
	}
	
	public void appendTo(StringBuilder sb, boolean incl_epsilon) {
		sb.append("[ ");
		List<Symbol> l;
		if (incl_epsilon) l = data_incl;
		else l = data_excl;
		for (int i = 0; i < l.size(); i++) {
			Symbol s = l.get(i);
			s.append(sb);
			sb.append(' ');
		}
		sb.append("]");
	}

	/**
	 * @return the same result as a call of <code>get(index, false)</code>
	 */
	public Symbol get(int index) {
		return get(index, false);
	}
	
	public Symbol get(int index, boolean incl_epsilon) {
		if (index < 0) return null;
		List<Symbol> l;
		if (incl_epsilon) l = data_incl;
		else l = data_excl;
		if (index >= l.size()) return null;
		return l.get(index);
	}

	/**
	 * @return the index of the exclIndex'th non-epsilon symbol within the sequence of symbols containing
	 *         epsilon symbols
	 */
	public int getInclIndexOf(int exclIndex) {
		int currentIncl = 0;
		int currentExcl = -1;
		do {
			while (data_incl.get(currentIncl) instanceof Epsilon)
				currentIncl++;
			currentExcl++;
		} while (currentExcl < exclIndex);
		return currentIncl;
	}

	public Iterator<Symbol> iterator() {
		return data_excl.iterator();
	}
	
	/**
	 * @return the same result as a call of <code>length(false)</code>
	 */
	public int length() {
		return length(false);
	}

	public int length(boolean incl_epsilon) {
		List<Symbol> l;
		if (incl_epsilon) l = data_incl;
		else l = data_excl;
		return l.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		appendTo(sb, true);
		return sb.toString();
	}

	public List<Integer> contains(SymbolSequence that) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		int thissize = this.data_excl.size();
		int thatsize = that.data_excl.size();
		if (thissize >= thatsize) {
			for (int pos = 0; pos < thissize; pos++) {
				for (int cmp = 0; cmp < min(thatsize, thissize - pos); cmp++) {
					Symbol s1 = this.data_excl.get(pos + cmp);
					Symbol s2 = that.data_excl.get(cmp);
					if (!s1.equals(s2)) break;
					if (cmp == thatsize - 1) result.add(pos);
				}
			}
		}
		return result;
	}

	private int min(int x, int y) {
		if (x < y) return x;
		return y;
	}
	
	public SymbolSequence replace(SymbolSequence repl, int pos, SymbolSequence by) {
		SymbolSequence result = new SymbolSequence();
		for (int i = 0; i < pos; i++) {
			result.append(get(i, false));
		}
		result.append(by);
		for (int i = pos + repl.length(false); i < length(false); i++) {
			result.append(get(i, false));
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof SymbolSequence)) return false;
		SymbolSequence that = (SymbolSequence) o;
		if (this.data_excl.size() != that.data_excl.size()) return false;
		for (int i = 0; i < this.data_excl.size(); i++) {
			Symbol s1 = this.data_excl.get(i);
			Symbol s2 = that.data_excl.get(i);
			if (!s1.equals(s2)) return false;
		}
		return true;
	}

	private void hash() {
		for (int i = 0; i < data_excl.size(); i++) {
			Symbol s = data_excl.get(i);
			hashcode ^= s.hashCode() * i;
		}
	}

	@Override
	public int hashCode() {
		if (hashcode == 0) hash();
		return hashcode;
	}

	public int compareTo(SymbolSequence that) {
		if (this == that) return 0;
		int end = min(this.data_excl.size(), that.data_excl.size());
		for (int i = 0; i < end; i++) {
			Symbol s1 = this.data_excl.get(i);
			Symbol s2 = that.data_excl.get(i);
			int result = s1.compareTo(s2);
			if (result != 0) return result;
		}
		if (this.data_excl.size() < that.data_excl.size()) return -1;
		if (this.data_excl.size() > that.data_excl.size()) return 1;
		return 0;
	}

}
