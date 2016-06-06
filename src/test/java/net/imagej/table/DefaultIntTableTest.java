/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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

package net.imagej.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Tests {@link DefaultIntTable}.
 *
 * @author Alison Walter
 */
public class DefaultIntTableTest {

	private static final String[] HEADERS = { "Header1", "Header2", "Header3",
		"Header4", "Header5", "Header6" };

	private static final int[][] DATA = {
		{ 127, -45, 605, -123440804, -4082, 57823 },
		{ 0, 0, 9, 12, 856, 1036 },
		{ 17, 77, -684325, 894, -3246, 423 },
		{ -3, -5234, 97, 93726, 672, -2 },
		{ 4, 6, -22222222, 56234, -934270, -1938475430 },
		{ 2147483647, -104, 867, -8, 386443263, 1248 },
		{ 12, -2147483648, 456, 4652, -17, 95 },
		{ 9275, -676, 7, 134, -32176368, 759 },
		{ 184, 56, 104920256, 1436437635, -435, 1 },
		{ 67, 3, -9, 94754, 4, -287934657 },
		{ 48, -356, -748, -93784, 5879, 5 },
		{ 289, 546453765, 0, 0, -6456, -23455 },
		{ 768, -1411556, 7, 2356, 7925, 7468 },
		{ -45, 25367, 546, 6757, -3, 1645 },
		{ 1, 6, -2562345, -23565584, -35815, 956 },
	};

	@Test
	public void testStructure() {
		final IntTable table = createTable();
		// Check table size
		assertEquals(6, table.getColumnCount());
		assertEquals(15, table.getRowCount());
		for (final IntColumn column : table) {
			assertEquals(15, column.size());
		}

		// Test headers
		for (int n = 0; n < table.getColumnCount(); n++) {
			assertEquals(table.getColumnHeader(n), HEADERS[n]);
		}

		// Test getting columns
		for (int c = 0; c < table.getColumnCount(); c++) {
			final IntColumn columnByHeader = table.get(HEADERS[c]);
			final IntColumn columnByIndex = table.get(c);
			assertSame(columnByHeader, columnByIndex);
			assertEquals(DATA.length, columnByHeader.size());
			// Test columns have expected row values
			for (int r = 0; r < table.getRowCount(); r++) {
				assertEquals(DATA[r][c], table.getValue(c, r));
				assertEquals(DATA[r][c], columnByHeader.getValue(r));
			}
		}
	}

	@Test
	public void testGetColumnType() {
		final IntTable table = createTable();
		final IntColumn col = table.get(0);
		assertEquals(col.getType(), Integer.class);
	}

	// TODO - Add more tests.

	// -- Helper methods --

	private IntTable createTable() {
		final IntTable table = new DefaultIntTable(DATA[0].length, DATA.length);

		for (int c = 0; c < HEADERS.length; c++) {
			table.setColumnHeader(c, HEADERS[c]);
		}

		for (int r = 0; r < DATA.length; r++) {
			for (int c = 0; c < DATA[r].length; c++) {
				table.setValue(c, r, DATA[r][c]);
			}
		}

		return table;
	}

}