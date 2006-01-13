package ch0p.datastrutures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ch0p.datastrutures.Rule.Match;



/**
 * An encapsulation for List&lt;Application&gt;, providing custom equals(Object), hashValue(), and
 * compareTo(Derivation) method implementations
 * 
 * @author Tom Gelhausen
 * @version $Id$
 */
public class Derivation implements List<Rule.Match>, Comparable<Derivation> {
	private int hv = 0;

	List<Rule.Match> list;
	
	public Derivation(Rule.Match[] aa) {
		list = new ArrayList<Rule.Match>();
		for (Rule.Match a : aa) {
			list.add(a);
			hv ^= a.hashCode();
		}
	}

	public Derivation(List<Rule.Match> la) {
		list = la;
		for (int i = 0; i < list.size(); i++) {
			hv ^= list.get(i).hashCode();
		}
	}

	public int compareTo(Derivation that) {
		int end = min(this.list.size(), that.list.size());
		for (int i = 0; i < end; i++) {
			Rule.Match a1 = this.list.get(i);
			Rule.Match a2 = that.list.get(i);
			int r = a1.compareTo(a2);
			if (r != 0) return r;
		}
		if (this.list.size() < that.list.size()) return -1;
		if (this.list.size() > that.list.size()) return 1;
		return 0;
	}

	/**
	 * 2 Derivation are equal iff the lists are of the same length and all elements are equal
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Derivation)) return false;
		Derivation that = (Derivation) o;
		if (this.list.size() != that.list.size()) return false;
		for (int i = 0; i < this.list.size(); i++) {
			if (!this.list.get(i).equals(that.list.get(i))) return false;
		}
		return true;
	}

	/**
	 * @return an array containing the elements of the list in the same order
	 */
/*	public Rule.Match[] toArray() {
		Rule.Match[] aa = new Rule.Match[list.size()];
		for (int j = 0; j < list.size(); j++) {
			aa[j] = list.get(j);
		}
		return aa;
	}
*/	
	/**
	 * @return a precomuted hash value. the value is computed during initialization, so don't change the
	 *         list after initialization!
	 */
	public int hashValue() {
		return hv;
	}

	@Override
	public String toString() {
		return "h=" + hv + ", " + list.toString();
	}

	private final int min(int a, int b) {
		if (a < b) return a;
		return b;
	}

	public void add(int index, Match element) {
		list.add(index, element);
	}

	public boolean add(Match o) {
		return list.add(o);
	}

	public boolean addAll(Collection<? extends Match> c) {
		return list.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends Match> c) {
		return list.addAll(index, c);
	}

	public void clear() {
		list.clear();
	}

	public boolean contains(Object o) {
		return list.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	public Match get(int index) {
		return list.get(index);
	}

	public int hashCode() {
		return list.hashCode();
	}

	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<Match> iterator() {
		return list.iterator();
	}

	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	public ListIterator<Match> listIterator() {
		return list.listIterator();
	}

	public ListIterator<Match> listIterator(int index) {
		return list.listIterator(index);
	}

	public Match remove(int index) {
		return list.remove(index);
	}

	public boolean remove(Object o) {
		return list.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	public Match set(int index, Match element) {
		return list.set(index, element);
	}

	public int size() {
		return list.size();
	}

	public List<Match> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}
}