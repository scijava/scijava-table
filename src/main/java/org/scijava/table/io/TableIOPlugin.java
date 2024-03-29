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

package org.scijava.table.io;

import java.io.IOException;

import org.scijava.io.IOPlugin;
import org.scijava.io.location.Location;
import org.scijava.table.Table;

/**
 * Abstract plugin class for saving and loading tables
 *
 * @author Deborah Schmidt
 */
@SuppressWarnings("rawtypes")
public interface TableIOPlugin extends IOPlugin<Table> {

	@Override
	default Table<?, ?> open(Location source) throws IOException {
		return open(source, new TableIOOptions());
	}

	/**
	 * Opens data from the given source.
	 *
	 * @param source The data source to open as a table.
	 * @param options The options to use when opening the data.
	 * @throws IOException If something goes wrong opening the data source.
	 */
	default Table<?, ?> open(final Location source, final TableIOOptions options)
		throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	default void save(Table data, Location destination) throws IOException {
		save(data, destination, new TableIOOptions());
	}

	/**
	 * Saves the given data to the specified destination.
	 *
	 * @param data The table to save.
	 * @param destination The destination where the table should be saved.
	 * @param options The options to use when saving the data.
	 * @throws IOException If something goes wrong opening the data source.
	 */
	default void save(final Table<?, ?> data, final Location destination,
		final TableIOOptions options) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	default Class<Table> getDataType() {
		return Table.class;
	}
}
