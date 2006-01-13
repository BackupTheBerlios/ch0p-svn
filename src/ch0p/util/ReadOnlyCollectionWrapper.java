/*
 * Created on 30.11.2005
 */
package ch0p.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Tom Gelhausen
 */
public final class ReadOnlyCollectionWrapper<T> implements Collection<T> {
	private class MyIter implements Iterator<T> {
		private final Iterator<? extends T> myIter;
		public MyIter(Iterator<? extends T> iter) {
			myIter = iter;
		}
		public final boolean hasNext() {
			return myIter.hasNext();
		}
		public final T next() {
			return myIter.next();
		}
		public final void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private final Collection< ? extends T> data;

	public ReadOnlyCollectionWrapper(Collection< ? extends T> c) {
		data = c;
	}

	public boolean add(@SuppressWarnings("unused") T o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(@SuppressWarnings("unused") Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean contains(Object o) {
		return data.contains(o);
	}

	public boolean containsAll(Collection< ? > c) {
		return data.containsAll(c);
	}

	@Override
	public boolean equals(Object o) {
		return data.equals(o);
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}
	
	public boolean isEmpty() {
		return data.isEmpty();
	}

	public Iterator<T> iterator() {
		return new MyIter(data.iterator());
	}

	public boolean remove(Object o) {
		return data.remove(o);
	}

	public boolean removeAll(Collection< ? > c) {
		return data.removeAll(c);
	}

	public boolean retainAll(@SuppressWarnings("unused") Collection< ? > c) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return data.size();
	}

	public Object[] toArray() {
		return data.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return data.toArray(a);
	}
}
