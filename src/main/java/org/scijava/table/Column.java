/*
 * #%L
 * Table structures for SciJava.
 * %%
 * Copyright (C) 2012 - 2022 Board of Regents of the University of
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.scijava.util.Sizable;

/**
 * A column of data of a {@link Table}.
 *
 * @author Curtis Rueden
 * @param <T> The type of data stored in the table.
 */
public interface Column<T> extends List<T>, Sizable {

	/** Gets the header of this column. */
	String getHeader();

	/** Sets the header of this column. */
	void setHeader(String header);

	/** Gets the column's size (i.e., number of rows). */
	@Override
	int size();

	/** Sets the column's size (i.e., number of rows). */
	@Override
	void setSize(int size);

	/** Returns the actual type of data stored in the column. */
	Class<T> getType();

	// -- List and Collection methods --

	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default boolean contains(final Object o) {
		return indexOf(o) >= 0;
	}

	@Override
	default Iterator<T> iterator() {
		return listIterator();
	}

	@Override
	default Object[] toArray() {
		return SimpleCollections.toArray(this);
	}

	@Override
	default <E> E[] toArray(E[] a) {
		return SimpleCollections.toArray(this, a);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return SimpleCollections.containsAll(this, c);
	}

	@Override
	default int indexOf(Object o) {
		return SimpleCollections.indexOf(this, o);
	}

	@Override
	default int lastIndexOf(Object o) {
		return SimpleCollections.lastIndexOf(this, o);
	}

	@Override
	default ListIterator<T> listIterator() {
		return listIterator(0);
	}

	@Override
	default ListIterator<T> listIterator(final int index) {
		return SimpleCollections.listIterator(this, index);
	}

	@Override
	default List<T> subList(int fromIndex, int toIndex) {
		return SimpleCollections.subList(this, fromIndex, toIndex);
	}
}
