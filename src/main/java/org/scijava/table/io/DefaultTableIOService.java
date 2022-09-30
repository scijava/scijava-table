/*-
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
import java.net.URISyntaxException;

import org.scijava.io.AbstractTypedIOService;
import org.scijava.io.IOPlugin;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;
import org.scijava.table.Table;

@Plugin(type = Service.class)
public class DefaultTableIOService extends AbstractTypedIOService<Table<?, ?>>
	implements TableIOService
{

	@Override
	public boolean canOpen(final Location source) {
		final IOPlugin<?> opener = ioService().getOpener(source);
		if (opener == null) return false;
		return Table.class.isAssignableFrom(opener.getDataType());
	}

	@Override
	public Table<?, ?> open(final String source, final TableIOOptions options)
		throws IOException
	{
		try {
			return open(locationService().resolve(source), options);
		}
		catch (final URISyntaxException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Table<?, ?> open(final Location source, final TableIOOptions options)
		throws IOException
	{
		final IOPlugin<?> opener = ioService().getOpener(source);
		if (opener != null && Table.class.isAssignableFrom(opener.getDataType()) &&
			TableIOPlugin.class.isAssignableFrom(opener.getClass()))
		{
			return ((TableIOPlugin) opener).open(source, options);
		}
		throw new UnsupportedOperationException("No compatible opener found.");
	}

	@Override
	public void save(final Table<?, ?> table, final String destination,
		final TableIOOptions options) throws IOException
	{
		try {
			save(table, locationService().resolve(destination), options);
		}
		catch (final URISyntaxException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void save(final Table<?, ?> table, final Location destination,
		final TableIOOptions options) throws IOException
	{
		@SuppressWarnings("rawtypes")
		final IOPlugin<Table> saver = ioService().getSaver(table, destination);
		if (saver != null && TableIOPlugin.class.isAssignableFrom(saver
			.getClass()))
		{
			((TableIOPlugin) saver).save(table, destination, options);
		}
		else {
			throw new UnsupportedOperationException("No compatible saver found.");
		}
	}

}
