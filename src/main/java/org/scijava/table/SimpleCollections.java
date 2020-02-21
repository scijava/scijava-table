/*
 * #%L
 * Table structures for SciJava.
 * %%
 * Copyright (C) 2012 - 2020 Board of Regents of the University of
 * Wisconsin-Madison, and Friedrich Miescher Institute for Biomedical Research.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.table;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Simple default implementations of {@link Collection} and {@link List}
 * methods.
 *
 * @author Curtis Rueden
 */
final class SimpleCollections {

	private SimpleCollections() {
		// NB: Prevent instantiation of utility class.
	}

	static Object[] toArray(final List<?> list) {
		final Object[] array = new Object[list.size()];
		for (int i = 0; i < array.length; i++)
			array[i] = list.get(i);
		return array;
	}

	@SuppressWarnings("unchecked")
	static <A> A[] toArray(final List<?> list, final A[] a) {
		final A[] array = a.length >= list.size() ? a : //
			(A[]) Array.newInstance(a.getClass().getComponentType(), list.size());
		for (int i = 0; i < list.size(); i++)
			array[i] = (A) list.get(i);
		return array;
	}

	static <E> boolean add(final List<E> list, final E o) {
		list.add(list.size(), o);
		return true;
	}

	static boolean remove(final List<?> list, final Object o) {
		final int index = list.indexOf(o);
		if (index < 0) return false;
		list.remove(index);
		return true;
	}

	static boolean containsAll(final Collection<?> collection,
		final Collection<?> c)
	{
		for (final Object o : c)
			if (!collection.contains(o)) return false;
		return true;
	}

	static <E> boolean addAll(final Collection<E> collection,
		final Collection<? extends E> c)
	{
		boolean changed = false;
		for (final E o : c)
			changed |= collection.add(o);
		return changed;
	}

	static <E> boolean addAll(final List<E> list, final int index,
		final Collection<? extends E> c)
	{
		int i = index;
		for (final E o : c)
			list.add(i++, o);
		return c.size() > 0;
	}

	static boolean removeAll(final Collection<?> collection,
		final Collection<?> c)
	{
		boolean changed = false;
		for (final Object o : c)
			changed |= collection.remove(o);
		return changed;
	}

	static boolean retainAll(final Collection<?> collection,
		final Collection<?> c)
	{
		final List<?> absent = collection.stream() //
			.filter(o -> !c.contains(o)) //
			.collect(Collectors.toList());
		return collection.removeAll(absent);
	}

	static int indexOf(final List<?> list, final Object o) {
		for (int i = 0; i < list.size(); i++)
			if (Objects.equals(list.get(i), o)) return i;
		return -1;
	}

	static int lastIndexOf(final List<?> list, final Object o) {
		for (int i = list.size() - 1; i >= 0; i--)
			if (Objects.equals(list.get(i), o)) return i;
		return -1;
	}

	static <E> ListIterator<E> listIterator(final List<E> list, final int index) {
		return new ListIterator<E>() {

			private int i = index;
			private int last = -1;

			@Override
			public boolean hasNext() {
				return i < list.size();
			}

			@Override
			public E next() {
				return list.get(last = i++);
			}

			@Override
			public boolean hasPrevious() {
				return i > 0;
			}

			@Override
			public E previous() {
				return list.get(last = --i);
			}

			@Override
			public int nextIndex() {
				return i;
			}

			@Override
			public int previousIndex() {
				return i - 1;
			}

			@Override
			public void remove() {
				if (last < 0) throw new IllegalStateException();
				list.remove(last);
				last = -1;
			}

			@Override
			public void set(final E e) {
				if (last < 0) throw new IllegalStateException();
				list.set(last, e);
			}

			@Override
			public void add(final E e) {
				list.add(i++, e);
				last = -1;
			}
		};
	}

	static <E> List<E> subList(final List<E> list, final int fromIndex,
		final int toIndex)
	{
		return new AbstractList<E>() {

			@Override
			public E get(final int index) {
				return list.get(index + fromIndex);
			}

			@Override
			public int size() {
				return toIndex - fromIndex;
			}
		};
	}
}
