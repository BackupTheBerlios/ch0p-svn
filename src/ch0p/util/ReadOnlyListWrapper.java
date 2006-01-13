/*
 * Created on 30.11.2005
 */
package ch0p.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Tom Gelhausen
 */
public final class ReadOnlyListWrapper<T> implements List<T> {
	private class MyIter implements Iterator<T> {
		private final Iterator< ? extends T> myIter;

		public MyIter(Iterator< ? extends T> iter) {
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

	private class MyListIter implements ListIterator<T> {
		private final ListIterator< ? extends T> myIter;

		public MyListIter(ListIterator< ? extends T> iter) {
			myIter = iter;
		}

		public void add(@SuppressWarnings("unused") T o) {
			throw new UnsupportedOperationException();
		}

		public boolean hasNext() {
			return myIter.hasNext();
		}

		public boolean hasPrevious() {
			return myIter.hasPrevious();
		}

		public T next() {
			return myIter.next();
		}

		public int nextIndex() {
			return myIter.nextIndex();
		}

		public T previous() {
			return myIter.previous();
		}

		public int previousIndex() {
			return myIter.previousIndex();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void set(@SuppressWarnings("unused") T o) {
			throw new UnsupportedOperationException();
		}
	}

	private final List< ? extends T> data;

	public ReadOnlyListWrapper(List< ? extends T> c) {
		data = c;
	}

	public void add(@SuppressWarnings("unused") int index, @SuppressWarnings("unused") T element) {
		throw new UnsupportedOperationException();
	}

	public boolean add(@SuppressWarnings("unused") T o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(@SuppressWarnings("unused") Collection< ? extends T> c) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(@SuppressWarnings("unused") int index, @SuppressWarnings("unused") Collection< ? extends T> c) {
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

	public T get(int index) {
		return data.get(index);
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	public int indexOf(Object o) {
		return data.indexOf(o);
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public Iterator<T> iterator() {
		return new MyIter(data.iterator());
	}

	public int lastIndexOf(Object o) {
		return data.lastIndexOf(o);
	}

	public ListIterator<T> listIterator() {
		return new MyListIter(data.listIterator());
	}

	public ListIterator<T> listIterator(int index) {
		return new MyListIter(data.listIterator(index));
	}

	public T remove(@SuppressWarnings("unused") int index) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(@SuppressWarnings("unused") Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(@SuppressWarnings("unused") Collection< ? > c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(@SuppressWarnings("unused") Collection< ? > c) {
		throw new UnsupportedOperationException();
	}

	public T set(@SuppressWarnings("unused") int index, @SuppressWarnings("unused") T element) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return data.size();
	}

	public List<T> subList(int fromIndex, int toIndex) {
		return new ReadOnlyListWrapper<T>(data.subList(fromIndex, toIndex));
	}

	public Object[] toArray() {
		return data.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return data.toArray(a);
	}
}
