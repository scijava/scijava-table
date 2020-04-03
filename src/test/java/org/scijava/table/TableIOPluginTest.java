/*-
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.IOPlugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.table.io.TableIOOptions;
import org.scijava.table.io.TableIOPlugin;
import org.scijava.table.io.TableIOService;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TableIOPluginTest {

	private Context context;

	@SuppressWarnings("rawtypes")
	@Before
	public void setUp() {
		context = new Context();
		PluginInfo<IOPlugin> info = PluginInfo.create(FakeTableIOPlugin.class,
			IOPlugin.class);
		context.service(PluginService.class).addPlugin(info);
	}

	@After
	public void tearDown() {
		context.dispose();
		context = null;
	}

	@Test
	public void testTableIOService() {
		String tableFile = "fakeTableFile.csv";
		TableIOService tableIOService = context.getService(TableIOService.class);
		try {
			Table<?, ?> data = tableIOService.open(tableFile);
			assertTrue(Table.class.isAssignableFrom(data.getClass()));
		}
		catch (IOException exc) {
			fail(exc.toString());
		}
	}

	@Test
	public void testTableIOServiceWithOptions() {
		String tableFile = "fakeTableFile.csv";
		TableIOService tableIOService = context.getService(TableIOService.class);
		TableIOOptions options = new TableIOOptions()
				.readColumnHeaders(true)
				.readRowHeaders(true)
				.columnDelimiter('-')
				.columnType(0, Double.class);
		try {
			Table<?, ?> data = tableIOService.open(tableFile, options);
			assertTrue(Table.class.isAssignableFrom(data.getClass()));
		}
		catch (IOException exc) {
			fail(exc.toString());
		}
	}

	@SuppressWarnings("rawtypes")
	public static class FakeTableIOPlugin extends TableIOPlugin {

		@Override
		public boolean supportsOpen(String loc) {
			return loc.endsWith("csv");
		}

		@Override
		public boolean supportsSave(String loc) {
			return loc.endsWith("csv");
		}

		@Override
		public Table open(String loc) {
			return new DefaultGenericTable();
		}

		@Override
		public Table open(String loc, TableIOOptions options) {
			return new DefaultGenericTable();
		}
	}

}
