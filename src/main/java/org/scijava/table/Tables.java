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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility methods for constructing tables.
 * 
 * @author Curtis Rueden
 */
public final class Tables {

	private Tables() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Creates a table wrapping a list of maps. Each map is one row of the table.
	 * Map keys are column names; map values are cell data.
	 * <p>
	 * Note that columns are inferred from the first map/row of the table, not
	 * unioned across all rows.
	 * </p>
	 * 
	 * @param data The data to wrap. Each list element is a row; maps go from
	 *          column name to data.
	 * @param rowHeaders List of row header labels. Pass null for no row headers.
	 * @return A {@link Table} object wrapping the data structure.
	 */
	public static <T> Table<Column<T>, T> wrap(
		final List<? extends Map<?, ? extends T>> data, final List<String> rowHeaders)
	{
		if (data.isEmpty()) throw new IllegalArgumentException("Cannot wrap an empty list");

		return new ReadOnlyTable<T>() {

			@Override
			public int getRowCount() {
				return data.size();
			}

			@Override
			public String getRowHeader(final int row) {
				if (rowHeaders == null || rowHeaders.size() < row) return null;
				return rowHeaders.get(row);
			}

			@Override
			public int size() {
				return data.get(0).size();
			}

			@Override
			public Column<T> get(final int col) {
				class ColumnAccessor implements ReadOnlyColumn<T> {

					private final Object tableData = data;
					private boolean initialized;
					private String colHeader;

					@Override
					public String getHeader() {
						if (!initialized) {
							int c = 0;
							for (final Object key : data.get(0).keySet()) {
								if (col == c++) {
									colHeader = key == null ? "<null>" : key.toString();
									break;
								}
							}
							initialized = true;
						}
						return colHeader;
					}

					@Override
					public int size() {
						return data.size();
					}

					@Override
					public Class<T> getType() {
						// TODO: Consider whether this is terrible.
						throw new UnsupportedOperationException();
					}

					@Override
					public T get(final int index) {
						return data.get(index).get(getHeader());
					}

					@Override
					public int hashCode() {
						return getHeader().hashCode();
					}

					@Override
					public boolean equals(final Object obj) {
						if (!(obj instanceof ColumnAccessor)) return false;
						final ColumnAccessor other = ((ColumnAccessor) obj);
						if (data != other.tableData) return false;
						return Objects.equals(getHeader(), other.getHeader());
					}
				}
				return new ColumnAccessor();
			}
		};
	}

	// -- Internal methods --

	/**
	 * Gets the column index corresponding to the given header, throwing an
	 * exception if no such column exists.
	 */
	static int colIndex(final Table<?, ?> table, final String colHeader) {
		final int colIndex = table.getColumnIndex(colHeader);
		if (colIndex < 0) //
			throw new IllegalArgumentException("No such column: " + colHeader);
		return colIndex;
	}

	/** Throws an exception if the given column(s) are out of bounds. */
	static void checkCol(final Table<?, ?> table, final int col,
		final int count)
	{
		check("column", col, count, table.getColumnCount());
	}

	/** Throws an exception if the given row(s) are out of bounds. */
	static void checkRow(final Table<?, ?> table, final int row,
		final int count)
	{
		check("row", row, count, table.getRowCount());
	}

	/**
	 * Generics-friendly helper method for {@link Table#set(int, int, Object)} and
	 * {@link Table#set(String, int, Object)}.
	 */
	static <U> void assign(final Column<U> column, final int row,
		final Object value)
	{
		if (value != null && !column.getType().isInstance(value)) {
			throw new IllegalArgumentException("value of type " + value.getClass() +
				" is not a " + column.getType());
		}
		@SuppressWarnings("unchecked")
		final U typedValue = (U) value;
		column.set(row, typedValue);
	}

	// -- Helper methods --

	/** Throws an exception if the given values are out of bounds. */
	private static void check(final String name, final int index, final int count,
		final int bound)
	{
		final int last = index + count - 1;
		if (index >= 0 && last < bound) return;
		if (count <= 1) {
			throw new IndexOutOfBoundsException("Invalid " + name + ": " + index);
		}
		throw new IndexOutOfBoundsException("Invalid " + name + "s: " + index +
			" - " + last);
	}

	private static UnsupportedOperationException readOnlyException() {
		return new UnsupportedOperationException("Wrapped table is read-only");
	}

	// -- Helper classes --

	/** Read-only version of a {@link Table}. */
	private static interface ReadOnlyTable<T> extends Table<Column<T>, T> {

		@Override
		default void setColumnCount(final int colCount) {
			throw readOnlyException();
		}

		@Override
		default List<Column<T>> insertColumns(final int col, final int count) {
			throw readOnlyException();
		}

		@Override
		default void setRowCount(final int rowCount) {
			throw readOnlyException();
		}

		@Override
		default void removeRows(int row, int count) {
			throw readOnlyException();
		}

		@Override
		default void setRowHeader(int row, String header) {
			throw readOnlyException();
		}

		@Override
		default void set(String colHeader, int row, T value) {
			throw readOnlyException();
		}

		@Override
		default boolean add(final Column<T> column) {
			throw readOnlyException();
		}

		@Override
		default boolean remove(final Object column) {
			throw readOnlyException();
		}

		@Override
		default boolean addAll(Collection<? extends Column<T>> c) {
			throw readOnlyException();
		}

		@Override
		default boolean addAll(int col, Collection<? extends Column<T>> c) {
			throw readOnlyException();
		}

		@Override
		default boolean removeAll(Collection<?> c) {
			throw readOnlyException();
		}

		@Override
		default boolean retainAll(Collection<?> c) {
			throw readOnlyException();
		}

		@Override
		default void clear() {
			throw readOnlyException();
		}

		@Override
		default Column<T> set(int col, Column<T> column) {
			throw readOnlyException();
		}

		@Override
		default void add(int col, Column<T> column) {
			throw readOnlyException();
		}

		@Override
		default Column<T> remove(int col) {
			throw readOnlyException();
		}
	}

	/** Read-only version of a {@link Column}. */
	private static interface ReadOnlyColumn<T> extends Column<T> {

		@Override
		default boolean add(final T e) {
			throw readOnlyException();
		}

		@Override
		default boolean remove(Object o) {
			throw readOnlyException();
		}

		@Override
		default boolean addAll(Collection<? extends T> c) {
			throw readOnlyException();
		}

		@Override
		default boolean addAll(int index, Collection<? extends T> c) {
			throw readOnlyException();
		}

		@Override
		default boolean removeAll(Collection<?> c) {
			throw readOnlyException();
		}

		@Override
		default boolean retainAll(Collection<?> c) {
			throw readOnlyException();
		}

		@Override
		default void clear() {
			throw readOnlyException();
		}

		@Override
		default T set(int index, T element) {
			throw readOnlyException();
		}

		@Override
		default void add(int index, T element) {
			throw readOnlyException();
		}

		@Override
		default T remove(int index) {
			throw readOnlyException();
		}

		@Override
		default void setHeader(String header) {
			throw readOnlyException();
		}

		@Override
		default void setSize(int size) {
			throw readOnlyException();
		}
	}
}
