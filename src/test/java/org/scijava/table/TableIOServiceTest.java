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

package org.scijava.table;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.table.io.DefaultTableIOService;
import org.scijava.table.io.TableIOService;

public class TableIOServiceTest {

	private Context context;

	@Before
	public void setUp() {
		context = new Context();
	}

	@After
	public void tearDown() {
		context.dispose();
		context = null;
	}

	@Test
	public void testTableIOService() {
		String tableFile = "fakeTableFile.csv";
		GenericTable table = new DefaultGenericTable();
		TableIOService tableIOService = context.getService(TableIOService.class);
		assertTrue(tableIOService.getClass().equals(DefaultTableIOService.class));
		assertTrue(tableIOService.canOpen(tableFile));
		assertTrue(tableIOService.canSave(table, tableFile));
		try {
			Table<?, ?> data = tableIOService.open(tableFile);
			assertTrue(Table.class.isAssignableFrom(data.getClass()));
		}
		catch (IOException exc) {
			fail(exc.toString());
		}
	}
}
