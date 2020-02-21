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

import java.util.ArrayList;
import java.util.Collection;

import org.scijava.util.SizableArrayList;

/**
 * Abstract superclass for column-oriented {@link Table} implementations.
 * 
 * @author Curtis Rueden
 * @param <T> The type of data stored in the table.
 */
public abstract class AbstractTable<C extends Column<? extends T>, T> extends
	SizableArrayList<C> implements Table<C, T>
{

	/** Header for each row in the table. Lazily populated. */
	private final SizableArrayList<String> rowHeaders;

	/** Number of rows in the table. */
	private int rowCount;

	/** Creates an empty table. */
	public AbstractTable() {
		this(0, 0);
	}

	/** Creates a table with the given column and row dimensions. */
	public AbstractTable(final int colCount, final int rowCount) {
		super();
		if (rowCount < 0) //
			throw new IllegalArgumentException("Invalid row count: " + rowCount);
		rowHeaders = new SizableArrayList<>();
		this.rowCount = rowCount;
		setColumnCount(colCount);
	}

	// -- Table methods --

	@Override
	public void setColumnCount(final int colCount) {
		if (colCount < 0) //
			throw new IllegalArgumentException("Invalid column count: " + colCount);
		setSize(colCount);
		scaleColumns();
	}

	@Override
	public ArrayList<C> insertColumns(final int col, final int count) {
		Tables.checkCol(this, col, 0);
		final int oldColCount = getColumnCount();
		final int newColCount = oldColCount + count;

		// expand columns list
		setColumnCount(newColCount);

		// copy columns after the inserted range into the new position
		for (int oldC = oldColCount - 1; oldC >= col; oldC--) {
			final int newC = oldC + count;
			set(newC, get(oldC));
		}

		// insert new blank columns
		final ArrayList<C> result = new ArrayList<>(count);
		for (int c = 0; c < count; c++) {
			final C column = createColumn(null);
			// initialize array
			column.setSize(getRowCount());
			result.add(column);
			set(col + c, column);
		}

		return result;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public void setRowCount(final int rowCount) {
		if (rowCount < 0) //
			throw new IllegalArgumentException("Invalid row count: " + rowCount);
		this.rowCount = rowCount;
		scaleColumns();
	}

	@Override
	public void removeRows(final int row, final int count) {
		Tables.checkRow(this, row, count);
		final int oldRowCount = getRowCount();
		final int newRowCount = oldRowCount - count;
		// copy data after the deleted range into the new position
		for (int oldR = row+count; oldR < oldRowCount; oldR++) {
			final int newR = oldR - count;
			setRowHeader(newR, getRowHeader(oldR));
			for (int c = 0; c < getColumnCount(); c++) {
				set(c, newR, get(c, oldR));
			}
		}
		setRowCount(newRowCount);
		// trim row headers list, if needed
		if (rowHeaders.size() > newRowCount) rowHeaders.setSize(newRowCount);
	}

	@Override
	public String getRowHeader(final int row) {
		Tables.checkRow(this, row, 1);
		if (rowHeaders.size() <= row) return null; // label not initialized
		return rowHeaders.get(row);
	}

	@Override
	public void setRowHeader(final int row, final String header) {
		Tables.checkRow(this, row, 1);
		if (row >= rowHeaders.size()) {
			// ensure row headers list is long enough to accommodate the header
			rowHeaders.setSize(row + 1);
		}
		// update the row header value, where applicable
		rowHeaders.set(row, header);
	}

	@Override
	public void set(final String colHeader, final int row, final T value) {
		final int col = Tables.colIndex(this, colHeader);
		Tables.checkRow(this, row, 1);
		Tables.assign((Column<?>) get(col), row, value);
	}

	// -- List methods --

	@Override
	public boolean add(final C column) {
		if (column.size() > rowCount) rowCount = column.size();
		scaleColumns();
		return super.add(column);
	}

	@Override
	public void add(final int col, final C column) {
		super.add(col, column);
		if (column.size() > rowCount) rowCount = column.size();
		scaleColumns();
	}

	@Override
	public boolean addAll(final Collection<? extends C> c) {
		for (final C column : c) {
			if (column.size() > rowCount) rowCount = column.size();
		}
		scaleColumns();
		return super.addAll(c);
	}

	@Override
	public boolean addAll(final int col, final Collection<? extends C> c) {
		for (final C column : c) {
			if (column.size() > rowCount) rowCount = column.size();
		}
		scaleColumns();
		return super.addAll(col, c);
	}

	// -- Internal methods --

	protected abstract C createColumn(final String colHeader);

	// -- Helper methods --

	/** Initializes and scales all columns to match the row count. */
	private void scaleColumns() {
		for (int c = 0; c < getColumnCount(); c++) {
			if (get(c) == null) {
				// initialize a new column
				set(c, createColumn(null));
			}
			get(c).setSize(getRowCount());
		}
	}
}
