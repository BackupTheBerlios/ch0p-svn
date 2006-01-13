/*
 * Created on 25.11.2005
 */
package ch0p.regex;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is an NFAState that may &quot;contain&quot; (represent) other NFAStates.
 * @author Tom Gelhausen
 *
 */
class MacroState extends NFAState implements Set<NFAState>{
	
	private static final String ErrorMsg = "This MacroState can not be used as set anymore.";
	
	private Set<NFAState> represents = new HashSet<NFAState>();
	private int hash = 0;

	protected MacroState() {
		super();
	}
	
	/**
	 * should be clear ;-)
	 */
	protected void finish() {
		if (represents.size()>0) {
			this.endState = false;
//			this.errorState = true;
//			for (NFAState s:represents) {
//				this.endState |= s.endState;
//				this.errorState &= s.errorState;
//			}
		}
		// allow garbage collector to remove old data
		// represents = null;
	}

	/* (non-Javadoc)
	 * @see java.util.Set#add(E)
	 */
	public boolean add(NFAState o) {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		hash ^= o.hashCode();
		return represents.add(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection< ? extends NFAState> c) {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		for (NFAState s:c) {
			if (s!=null)	hash ^= s.hashCode();
		}
		return represents.addAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#clear()
	 */
	public void clear() {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		hash = 0;
		represents.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.util.Set#isEmpty()
	 */
	public boolean isEmpty() {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		return represents.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#iterator()
	 */
	public Iterator<NFAState> iterator() {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		return represents.iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#size()
	 */
	public int size() {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		return represents.size();
	}
	
	@Override
	public boolean equals(Object o) {
		if (represents==null) return super.equals(o);
		if (!(o instanceof MacroState)) return false;
		MacroState that = (MacroState) o;
		if (this.represents.size()!=that.represents.size()) return false;
		return this.represents.containsAll(that.represents);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		return represents.contains(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection< ? > c) {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		return represents.containsAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		if (represents.contains(o)) hash ^= o.hashCode();
		return represents.remove(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection< ? > c) {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		for (Object o:c) {
			if (represents.contains(o)) hash ^= o.hashCode();
		}
		return represents.removeAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#retainAll(java.util.Collection)
	 */
	public boolean retainAll(@SuppressWarnings("unused") Collection< ? > c) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#toArray()
	 */
	public Object[] toArray() {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		return represents.toArray();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) {
		if (represents==null) throw new IllegalStateException(ErrorMsg);
		return represents.toArray(a);
	}

	@Override
	public String toString() {
		String s = super.toString();
		if (represents==null) return s;
		StringBuffer sb = new StringBuffer(s);
		sb.append(" {");
		boolean del = false;
		for (NFAState state:represents) {
			sb.append(state.getName());
			sb.append(",");
			del = true;
		}
		if (del) sb.delete(sb.length()-1, sb.length());
		sb.append("}");
		return sb.toString();
	}
}
