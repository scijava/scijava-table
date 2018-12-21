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

/**
 * Utility methods for constructing tables.
 * 
 * @author Curtis Rueden
 */
public final class Tables {

	private Tables() {
		// NB: Prevent instantiation of utility class.
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
}
