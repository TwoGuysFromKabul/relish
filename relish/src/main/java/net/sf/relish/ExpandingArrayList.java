package net.sf.relish;

import java.util.ArrayList;
import java.util.Collection;

/**
 * List that expands to required size if you try to add or set a value at an index that is beyond the current size. Also returns null from {@link #get(int)}
 * instead of throwing {@link IndexOutOfBoundsException}.
 */
public class ExpandingArrayList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 1L;

	/**
	 * @see java.util.ArrayList#set(int, java.lang.Object)
	 */
	@Override
	public E set(int index, E value) {

		expand(index + 1);
		return super.set(index, value);
	}

	/**
	 * @see java.util.ArrayList#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, E element) {

		expand(index);
		super.add(index, element);
	}

	/**
	 * @see java.util.ArrayList#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		expand(index);
		return super.addAll(index, c);
	}

	/**
	 * @see java.util.ArrayList#get(int)
	 */
	@Override
	public E get(int index) {

		if (index >= size()) {
			return null;
		}

		return super.get(index);
	}

	private void expand(int newSize) {
		while (size() < newSize) {
			add(null);
		}
	}
}
