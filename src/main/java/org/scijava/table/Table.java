/*
 * #%L
 * Table structures for SciJava.
 * %%
 * Copyright (C) 2012 - 2018 Board of Regents of the University of
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * A table of values.
 * 
 * @author Curtis Rueden
 * @param <C> The type of column used by the table.
 * @param <T> The type of data stored in the table.
 */
public interface Table<C extends Column<? extends T>, T> extends List<C> {

	/** Gets the number of columns in the table. */
	default int getColumnCount() {
		return size();
	}

	/** Sets the number of columns in the table. */
	void setColumnCount(int colCount);

	/** Returns the first column with the given header in the table. */
	default C get(final String colHeader) {
		return get(Tables.colIndex(this, colHeader));
	}

	/**
	 * Appends a column (with no header) to the table.
	 * 
	 * @return the column that was appended
	 */
	default C appendColumn() {
		return appendColumn(null);
	}

	/**
	 * Appends a column with the given header to the table.
	 * 
	 * @return the column that was appended
	 */
	default C appendColumn(final String colHeader) {
		return insertColumn(getColumnCount(), colHeader);
	}

	/**
	 * Appends a number of columns (with no headers) to the table.
	 * 
	 * @return the columns that were appended
	 */
	default List<C> appendColumns(final int count) {
		final ArrayList<C> result = new ArrayList<>(count);
		for (int c = 0; c < count; c++)
			result.add(appendColumn());
		return result;
	}

	/**
	 * Appends a block of columns with the given headers to the table.
	 * 
	 * @return the columns that were appended
	 */
	default List<C> appendColumns(final String... colHeaders) {
		final ArrayList<C> result = new ArrayList<>(colHeaders.length);
		for (final String colHeader : colHeaders) {
			result.add(appendColumn(colHeader));
		}
		return result;
	}

	/**
	 * Inserts a column (with no header) at the given position in the table.
	 * 
	 * @return the column that was inserted
	 */
	default C insertColumn(final int col) {
		return insertColumn(col, null);
	}

	/**
	 * Inserts a column with the specified header at the given position in the
	 * table.
	 * 
	 * @return the column that was inserted
	 */
	default C insertColumn(final int col, final String colHeader) {
		final List<C> result = insertColumns(col, 1);
		setColumnHeader(col, colHeader);
		return result.get(0);
	}

	/**
	 * Inserts a block of columns (with no headers) at the given position in the
	 * table.
	 * 
	 * @return the columns that were inserted
	 */
	List<C> insertColumns(int col, int count);

	/**
	 * Inserts a block of columns with the specified headers at the given position
	 * in the table.
	 * 
	 * @return the columns that were inserted
	 */
	default List<C> insertColumns(final int col, final String... headers) {
		// insert empty columns as a block
		final List<C> result = insertColumns(col, headers.length);

		// set headers for newly inserted columns
		for (int c = 0; c < headers.length; c++) {
			setColumnHeader(col + c, headers[c]);
		}
		return result;
	}

	/**
	 * Removes the column at the given position from the table.
	 * 
	 * @return the column that was removed
	 */
	default C removeColumn(final int col) {
		return remove(col);
	}

	/**
	 * Removes the first column with the given header from the table.
	 * 
	 * @return the column that was removed
	 */
	default C removeColumn(final String colHeader) {
		return removeColumn(Tables.colIndex(this, colHeader));
	}

	/**
	 * Removes a block of columns starting at the given position from the table.
	 * 
	 * @return the columns that were removed
	 */
	default List<C> removeColumns(final int col, final int count) {
		Tables.checkCol(this, col, count);

		// save to-be-removed columns
		final ArrayList<C> result = new ArrayList<>(count);
		for (int c = 0; c < count; c++) {
			result.add(get(col + c));
		}

		final int oldColCount = getColumnCount();
		final int newColCount = oldColCount - count;

		// copy data after the deleted range into the new position
		for (int oldC = col+count; oldC < oldColCount; oldC++) {
			final int newC = oldC - count;
			set(newC, get(oldC));
		}
		setColumnCount(newColCount);

		return result;
	}

	/**
	 * Removes the first columns with the given headers from the table.
	 * 
	 * @return the columns that were removed
	 */
	default List<C> removeColumns(final String... colHeaders) {
		final ArrayList<C> result = new ArrayList<>(colHeaders.length);
		for (final String colHeader : colHeaders) {
			result.add(removeColumn(colHeader));
		}
		return result;
	}

	/** Gets the number of rows in the table. */
	int getRowCount();

	/** Sets the number of rows in the table. */
	void setRowCount(int rowCount);

	/** Appends a row (with no header) to the table. */
	default void appendRow() {
		appendRow(null);
	}

	/** Appends a row with the given header to the table. */
	default void appendRow(final String header) {
		insertRow(getRowCount(), header);
	}

	/** Appends a block of rows (with no headers) to the table. */
	default void appendRows(final int count) {
		for (int c = 0; c < count; c++)
			appendRow();
	}

	/** Appends a block of rows with the given headers to the table. */
	default void appendRows(final String... headers) {
		for (final String header : headers)
			appendRow(header);
	}

	/** Inserts a row (with no header) at the given position in the table. */
	default void insertRow(final int row) {
		insertRow(row, null);
	}

	/**
	 * Inserts a row with the specified header at the given position in the table.
	 */
	default void insertRow(final int row, final String header) {
		insertRows(row, 1);
		setRowHeader(row, header);
	}

	/**
	 * Inserts a block of rows (with no headers) at the given position in the
	 * table.
	 */
	default void insertRows(final int row, final int count) {
		Tables.checkRow(this, row, 0);
		final int oldRowCount = getRowCount();
		final int newRowCount = oldRowCount + count;

		// expand rows list
		setRowCount(newRowCount);

		// copy data after the inserted range into the new position
		// NB: This loop goes backwards to prevent the same row from being copied
		// over and over again.
		for (int oldR = oldRowCount - 1; oldR >= row; oldR--) {
			final int newR = oldR + count;
			for (int c = 0; c < getColumnCount(); c++)
				set(c, newR, get(c, oldR));
		}

		// copy row headers after the inserted range into the new position
		// NB: This loop goes backwards for performance.
		// It ensures that rowHeaders is resized at most once.
		for (int oldR = oldRowCount - 1; oldR >= row; oldR--) {
			final int newR = oldR + count;
			setRowHeader(newR, getRowHeader(oldR));
		}

		// insert new blank row data
		for (int r = 0; r < count; r++) {
			for (int c = 0; c < getColumnCount(); c++)
				set(c, row + r, null);
		}

		// insert new blank row headers
		for (int r = 0; r < count; r++) {
			setRowHeader(row + r, null);
		}
	}


	/**
	 * Inserts a block of rows with the specified headers at the given position in
	 * the table.
	 */
	default void insertRows(final int row, final String... headers) {
		// insert empty rows as a block
		insertRows(row, headers.length);

		// set headers for newly inserted rows
		for (int r = 0; r < headers.length; r++) {
			setRowHeader(row + r, headers[r]);
		}
	}


	/** Removes the row at the given position from the table. */
	default void removeRow(final int row) {
		removeRows(row, 1);
	}

	/** Removes the first row with the given header from the table. */
	default void removeRow(final String header) {
		final int row = getColumnIndex(header);
		if (row < 0) throw new IndexOutOfBoundsException("No such row: " + header);
		removeRow(row);
	}

	/**
	 * Removes a block of rows starting at the given position from the table.
	 */
	void removeRows(int row, int count);

	/** Removes the first rows with the given headers from the table. */
	default void removeRows(final String... headers) {
		for (final String header : headers) {
			removeRow(header);
		}
	}

	/** Sets the number of columns and rows in the table. */
	default void setDimensions(final int colCount, final int rowCount) {
		setColumnCount(colCount);
		setRowCount(rowCount);
	}

	/** Gets the column header at the given column. */
	default String getColumnHeader(final int col) {
		return get(col).getHeader();
	}

	/** Sets the column header at the given column. */
	default void setColumnHeader(final int col, final String colHeader) {
		get(col).setHeader(colHeader);
	}

	/** Gets the column index of the column with the given header. */
	default int getColumnIndex(final String colHeader) {
		for (int c = 0; c < getColumnCount(); c++) {
			final String h = getColumnHeader(c);
			if (Objects.equals(h, colHeader)) return c;
		}
		return -1;
	}

	/** Gets the row header at the given row. */
	String getRowHeader(int row);

	/** Sets the row header at the given row. */
	void setRowHeader(int row, String header);

	/** Gets the row index of the row with the given header. */
	default int getRowIndex(final String header) {
		for (int r = 0; r < getRowCount(); r++) {
			final String h = getRowHeader(r);
			if (Objects.equals(h, header)) return r;
		}
		return -1;
	}

	/** Sets the table value at the given column and row. */
	default void set(final int col, final int row, final T value) {
		Tables.checkCol(this, col, 1);
		Tables.checkRow(this, row, 1);
		Tables.assign((Column<?>) get(col), row, value);
	}

	/** Sets the table value at the given column and row. */
	void set(String colHeader, int row, T value);

	/** Gets the table value at the given column and row. */
	default T get(final int col, final int row) {
		Tables.checkCol(this, col, 1);
		Tables.checkRow(this, row, 1);
		return get(col).get(row);
	}

	/** Gets the table value at the given column and row. */
	default T get(final String colHeader, final int row) {
		final int col = Tables.colIndex(this, colHeader);
		Tables.checkRow(this, row, 1);
		return get(col).get(row);
	}

	// -- List methods --

	/** Gets the number of columns in the table. */
	@Override
	int size();

	/** Gets whether the table is empty. */
	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Gets whether the table contains the given column.
	 * 
	 * @param column The {@link Column} whose presence in the table is to be
	 *          tested.
	 */
	@Override
	boolean contains(Object column);

	/** Returns an iterator over the columns in the table in proper sequence. */
	@Override
	Iterator<C> iterator();

	/**
	 * Returns an array containing all of the columns in the table in proper
	 * sequence (from first to last column).
	 */
	@Override
	Object[] toArray();

	/**
	 * Returns an array containing all of the column in the table in proper
	 * sequence (from first to last column); the runtime type of the returned
	 * array is that of the specified array. If the list of columns fits in the
	 * specified array, it is returned therein. Otherwise, a new array is
	 * allocated with the runtime type of the specified array and the size of this
	 * list of columns.
	 */
	@Override
	<A> A[] toArray(A[] a);

	/**
	 * Appends the specified column to the end of the table.
	 * <p>
	 * Updates the row count if this column has more rows than current table and
	 * scales the existing columns to have the same number of rows.
	 * </p>
	 */
	@Override
	boolean add(C column);

	/**
	 * Removes the first occurrence of the specified column from the table, if it
	 * is present.
	 * 
	 * @return <tt>true</tt> if the table contained the specified column
	 */
	@Override
	boolean remove(Object column);

	/**
	 * Returns <tt>true</tt> if the table contains all of the columns of the
	 * specified collection.
	 */
	@Override
	boolean containsAll(Collection<?> c);

	/**
	 * Appends all of the columns in the specified collection to the end of the
	 * table, in the order that they are returned by the specified collection's
	 * iterator.
	 * <p>
	 * Updates the row count if necessary, and scales the columns to match the row
	 * count if necessary.
	 * </p>
	 * 
	 * @return <tt>true</tt> if the table changed as a result of the call
	 */
	@Override
	boolean addAll(Collection<? extends C> c);

	/**
	 * Inserts all of the columns in the specified collection into this list at
	 * the specified position.
	 * <p>
	 * Updates the row count if necessary, and scales the columns to match the row
	 * count if necessary.
	 * </p>
	 * 
	 * @return <tt>true</tt> if the table changed as a result of the call
	 */
	@Override
	boolean addAll(int col, Collection<? extends C> c);

	/**
	 * Removes from the table all of its columns that are contained in the
	 * specified collection.
	 * 
	 * @return <tt>true</tt> if the table changed as a result of the call
	 */
	@Override
	boolean removeAll(Collection<?> c);

	/**
	 * Retains only the columns in the table that are contained in the specified
	 * collection. In other words, removes from the table all the columns that are
	 * not contained in the specified collection.
	 * 
	 * @return <tt>true</tt> if the table changed as a result of the call
	 */
	@Override
	boolean retainAll(Collection<?> c);

	/**
	 * Removes all data (including row and column headers) from the table. The
	 * table will be empty after this call returns.
	 * <p>
	 * If you want to retain the column headers, call {@link #setRowCount(int)}
	 * with a value of 0. If you want to retain the row headers, call
	 * {@link #setColumnCount(int)} with a value of 0.
	 * </p>
	 */
	@Override
	void clear();

	/** Returns the column at the specified position in the table. */
	@Override
	C get(int col);

	/**
	 * Replaces the column at the specified position in the table with the
	 * specified column.
	 * <p>
	 * No checking is done to ensure the new column has the same number of rows as
	 * the other existing columns.
	 * </p>
	 * 
	 * @return the column previously at the specified position
	 */
	@Override
	C set(int col, C column);

	/**
	 * Inserts the specified column at the specified position in the table.
	 * <p>
	 * Updates the row count if this column has more rows than current table and
	 * scales the existing columns to have the same number of rows.
	 * </p>
	 */
	@Override
	void add(int col, C column);

	/**
	 * Removes the column at the specified position in the table.
	 * 
	 * @return the column previously at the specified position
	 */
	@Override
	C remove(int col);

	/**
	 * Returns the index of the first occurrence of the specified column in the
	 * table, or -1 if the table does not contain the column.
	 */
	@Override
	int indexOf(Object column);

	/**
	 * Returns the index of the last occurrence of the specified column in the
	 * table, or -1 if the table does not contain the column.
	 */
	@Override
	int lastIndexOf(Object column);

	/**
	 * Returns a list iterator over the columns in the table (in proper sequence).
	 */
	@Override
	ListIterator<C> listIterator();

	/**
	 * Returns a list iterator of the columns in the table (in proper sequence),
	 * starting at the specified position in the table.
	 */
	@Override
	ListIterator<C> listIterator(int col);

	/**
	 * Returns a view of the portion of the table between the specified
	 * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive. The
	 * returned list is backed by the table, so non-structural changes in the
	 * returned list are reflected in the table, and vice-versa.
	 */
	@Override
	List<C> subList(int fromCol, int toCol);

}
