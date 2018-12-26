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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Tests {@link Tables}.
 *
 * @author Curtis Rueden
 */
public class TablesTest {

	@Test
	public void testWrap() {
		final List<Map<Object, Object>> towns = Arrays.asList( //
			map("Town", "Shanghai", "Population", 24_256_800), //
			map("Town", "Karachi", "Population", 23_500_000), //
			map("Town", "Beijing", "Population", 21_516_000), //
			map("Town", "Sao Paolo", "Population", 21_292_893));

		final List<String> rowHeaders = Arrays.asList("A", "B", "C", "D");

		final Table<?, ?> table = Tables.wrap(towns, rowHeaders);

		// check table dimensions
		assertEquals(2, table.size());
		assertEquals(2, table.getColumnCount());
		assertEquals(4, table.getRowCount());

		// check row headers
		for (int r = 0; r < rowHeaders.size(); r++)
			assertEquals(rowHeaders.get(r), table.getRowHeader(r));

		// check direct data access
		assertEquals("Shanghai", table.get(0, 0));
		assertEquals("Karachi", table.get(0, 1));
		assertEquals("Beijing", table.get(0, 2));
		assertEquals("Sao Paolo", table.get(0, 3));
		assertEquals(24_256_800, table.get(1, 0));
		assertEquals(23_500_000, table.get(1, 1));
		assertEquals(21_516_000, table.get(1, 2));
		assertEquals(21_292_893, table.get(1, 3));

		// check first column
		assertEquals("Town", table.getColumnHeader(0));
		final Column<?> townColumn = table.get("Town");
		assertEquals(4, townColumn.size());
		assertEquals("Shanghai", townColumn.get(0));
		assertEquals("Karachi", townColumn.get(1));
		assertEquals("Beijing", townColumn.get(2));
		assertEquals("Sao Paolo", townColumn.get(3));
		assertEquals(townColumn, table.get(0));

		// check second column
		assertEquals("Population", table.getColumnHeader(1));
		final Column<?> popColumn = table.get("Population");
		assertEquals(4, popColumn.size());
		assertEquals(24_256_800, popColumn.get(0));
		assertEquals(23_500_000, popColumn.get(1));
		assertEquals(21_516_000, popColumn.get(2));
		assertEquals(21_292_893, popColumn.get(3));
		assertEquals(popColumn, table.get(1));
	}

	private Map<Object, Object> map(final Object... kv) {
		final LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
		for (int i = 0; i < kv.length; i += 2)
			map.put(kv[i], kv[i + 1]);
		return map;
	}
}
