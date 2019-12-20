/*-
 * #%L
 * Table structures for SciJava.
 * %%
 * Copyright (C) 2012 - 2019 Board of Regents of the University of
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
import org.scijava.io.IOService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.table.Table;

@Plugin(type = Service.class)
public class DefaultTableIOService extends AbstractService implements
	TableIOService
{

	@Parameter
	private IOService ioService;

	@Override
	public boolean canOpen(String source) {
		IOPlugin<?> opener = ioService.getOpener(source);
		if (opener == null) return false;
		return Table.class.isAssignableFrom(opener.getDataType());
	}

	@Override
	public boolean canSave(Table<?, ?> table, String destination) {
		IOPlugin<Table<?, ?>> saver = ioService.getSaver(table, destination);
		if (saver == null) return false;
		return saver.supportsSave(destination);
	}

	@Override
	public Table<?, ?> open(String source) throws IOException {
		IOPlugin<?> opener = ioService.getOpener(source);
		if (opener != null && Table.class.isAssignableFrom(opener.getDataType())) {
			return (Table<?, ?>) opener.open(source);
		}
		throw new UnsupportedOperationException("No compatible opener found.");
	}

	@Override
	public void save(Table<?, ?> table, String destination) throws IOException {
		IOPlugin<Table<?, ?>> saver = ioService.getSaver(table, destination);
		if (saver != null) {
			saver.save(table, destination);
		}
		else {
			throw new UnsupportedOperationException("No compatible saver found.");
		}
	}
}
