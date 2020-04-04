/*
 * #%L
 * I/O plugins for SciJava table objects.
 * %%
 * Copyright (C) 2017 - 2020 Board of Regents of the University of
 * Wisconsin-Madison and University of Konstanz.
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.IOPlugin;
import org.scijava.io.IOService;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.util.ClassUtils;

/**
 * Tests for {@link DefaultTableIOPlugin}.
 * 
 * @author Leon Yang
 */
@SuppressWarnings("rawtypes")
public class DefaultTableIOPluginTest {

	private static final Context ctx = new Context();
	
	private List<File> tempFiles = new ArrayList<>();

	@Before
	@After
	public void removeTempFiles() {
		Location source = new FileLocation("fake.csv");
		new File(source.getURI()).delete();
		for (File f : tempFiles) {
			assertTrue(f.delete());
		}
	}

	/**
	 * Tests if the parser works on a common comma-delimited table.
	 */
	@Test
	public void testParser() throws IOException {
		final String[] colHeaders = {"col1", "col2", "col3", "col4", "col5"};
		final String[] rowHeaders = { null, null };
		final Double[][] content = { { 123.0, -123.0, 123.0, 123.0, 0.0 }, { 0.0,
			1234567890.0987654321, Double.NaN, Double.NEGATIVE_INFINITY, 0.0 } };
		final String[][] contentAsString = { { "123.0", "-123.0", "123.0", "123.0", "0.0" }, { "0.0",
				"1.2345678900987654E9", "NaN", "-Infinity", "0.0" } };

		final String expected = "col1,col2,col3,col4,col5\n" +
			"123.0,-123.0,123.0,123.0,0.0\n" +
			"0.0,1.2345678900987654E9,NaN,-Infinity,0.0\n";

		GenericTable table = new DefaultGenericTable();
		Arrays.stream(colHeaders).forEach(table::appendColumn);
		Arrays.stream(rowHeaders).forEach(table::appendRow);
		for (int i = 0; i < content.length; i++) {
			for (int j = 0; j < content[i].length; j++) {
				table.set(j, i, content[i][j]);
			}
		}

		// the table is populated with Double entries
		assertTableEquals(colHeaders, rowHeaders, content, table);

		final IOPlugin<Table> tableIO = ctx.service(IOService.class)
			.getInstance(DefaultTableIOPlugin.class);


		// by default, the parser creates comma separated entries
		assertEquals(expected, saveTable(table, tableIO));

		final Table table2 = openTable(expected, tableIO);

		// when opening the same table from disk, the default parser populates the table with String entries
		assertTableEquals(colHeaders, rowHeaders, contentAsString, table2);
	}

	/**
	 * Tests if quoting works in different senarios.
	 */
	@Test
	@Ignore // since it uses the setValue method, it modifies the DefaultTableIOPlugin parameters which makes other tests fail since they share a context
	public void testQuote() {
		final String[][] cells = { { "CORNER_TEXT",
			"' col  1 with white   spaces '", "'col 2 with ''QUOTE'' inside'",
			"'col 3 'connect,two' quoted strings'" }, { "should\tnot,break",
				"'unnecessary_quotes'", "should break" }, { "some,empty,cells", "",
					" ''" } };
		final String tableSource = makeTableSource(cells, " ", "\r\n");

		final String[] colHeaders = { " col  1 with white   spaces ",
			"col 2 with 'QUOTE' inside", "col 3 connect,two quoted strings" };
		final String[] rowHeaders = { "should\tnot,break", "some,empty,cells" };
		final String[][] content = { { "unnecessary_quotes", "should", "break" }, {
			"", "", "" } };

		final String expected = "CORNER_TEXT, col  1 with white   spaces ," +
			"'col 2 with ''QUOTE'' inside'," +
			"'col 3 connect,two quoted strings'\r\n" +
			"'should\tnot,break',unnecessary_quotes,should,break\r\n" +
			"'some,empty,cells','','',''\r\n";

		final IOPlugin<Table> tableIO = ctx.service(IOService.class)
			.getInstance(DefaultTableIOPlugin.class);
		try {
			setValues(tableIO, new String[] { "readColHeaders", "writeColHeaders",
				"readRowHeaders", "writeRowHeaders", "separator", "eol", "quote",
				"cornerText", "parser", "formatter" }, new Object[] { true, true, true,
					true, " ", "\r\n", '\'', "CORNER_TEXT", Function.identity(), Function
						.identity() });

			final Table table = openTable(tableSource, tableIO);
			assertTableEquals(colHeaders, rowHeaders, content, table);

			setValues(tableIO, new String[] { "separator" }, new Object[] { ',' });
			assertEquals(expected, saveTable(table, tableIO));
		}
		catch (final Exception exc) {
			exc.printStackTrace();
			fail(exc.getMessage());
		}
	}

	/**
	 * Tests if samll tables could be opened/saved correctly.
	 */
	@Test
	@Ignore // since it uses the setValue method, it modifies the DefaultTableIOPlugin parameters which makes other tests fail since they share a context
	public void testSmallTables() {
		final String[][] singleRow = { { "Row Header", "   3.1415926   " } };
		final String[][] singleCell = { { "   3.1415926   " } };
		final String[][] singleCol = { { "Col Header" }, { "   3.1415926   " } };
		final String[][] onlyRowHeader = { { "CORNER TEXT" }, { "Row Header" } };
		final String[][] onlyColHeader = { { "CORNER TEXT", "Col Header" } };
		final String[][] full = { { "CORNER TEXT", "Col Header" }, { "Row Header",
			"   3.1415926   " } };

		final String[] singleColHeader = { "Col Header" };
		final String[] singleRowHeader = { "Row Header" };
		final String[] emptyHeader = { null };
		final String[] empty = {};
		final Double[][] content = { { 3.1415926 } };
		final Double[][] emptyContent = { {} };

		final IOPlugin<Table> tableIO = ctx.service(IOService.class)
			.getInstance(DefaultTableIOPlugin.class);
		try {
			Table table;
			String expected;
			final Function<String, Double> parser = Double::valueOf;
			final Function<Double, String> formatter = val -> String.format("%.3f",
				val);
			setValues(tableIO, new String[] { "readColHeaders", "writeColHeaders",
				"readRowHeaders", "writeRowHeaders", "separator", "eol", "quote",
				"cornerText", "parser", "formatter" }, new Object[] { false, true, true,
					true, ",", "\n", "'", "CORNER TEXT", parser, formatter });
			table = openTable(makeTableSource(singleRow, ",", "\n"), tableIO);
			assertTableEquals(emptyHeader, singleRowHeader, content, table);
			expected = "Row Header,3.142\n";
			assertEquals(expected, saveTable(table, tableIO));

			setValues(tableIO, new String[] { "readRowHeaders" }, new Object[] {
				false });
			table = openTable(makeTableSource(singleCell, ",", "\n"), tableIO);
			assertTableEquals(emptyHeader, emptyHeader, content, table);
			expected = "3.142\n";
			assertEquals(expected, saveTable(table, tableIO));

			setValues(tableIO, new String[] { "readColHeaders" }, new Object[] {
				true });
			table = openTable(makeTableSource(singleCol, ",", "\n"), tableIO);
			assertTableEquals(singleColHeader, emptyHeader, content, table);
			expected = "Col Header\n3.142\n";
			assertEquals(expected, saveTable(table, tableIO));

			setValues(tableIO, new String[] { "readRowHeaders" }, new Object[] {
				true });
			table = openTable(makeTableSource(onlyColHeader, ",", "\n"), tableIO);
			assertTableEquals(singleColHeader, empty, emptyContent, table);
			expected = "Col Header\n";
			assertEquals(expected, saveTable(table, tableIO));

			table = openTable(makeTableSource(onlyRowHeader, ",", "\n"), tableIO);
			assertTableEquals(empty, singleRowHeader, emptyContent, table);
			expected = "Row Header\n";
			assertEquals(expected, saveTable(table, tableIO));

			table = openTable(makeTableSource(full, ",", "\n"), tableIO);
			assertTableEquals(singleColHeader, singleRowHeader, content, table);
			expected = "CORNER TEXT,Col Header\nRow Header,3.142\n";
			assertEquals(expected, saveTable(table, tableIO));
		}
		catch (final Exception exc) {
			exc.printStackTrace();
			fail(exc.getMessage());
		}

	}

	@Test(expected = IOException.class)
	public void testOpenNonExist() throws IOException {
		final IOPlugin<Table> tableIO = ctx.service(IOService.class)
			.getInstance(DefaultTableIOPlugin.class);
		tableIO.open("fake.csv");
	}

	// -- helper methods --

	/**
	 * Checks if a table has the expected column/row headers and content.
	 */
	private void assertTableEquals(final String[] colHeaders,
		final String[] rowHeaders, final Object[][] content,
		final Table table)
	{
		assertEquals(colHeaders.length, table.getColumnCount());
		assertEquals(rowHeaders.length, table.getRowCount());
		for (int c = 0; c < colHeaders.length; c++) {
			assertEquals(colHeaders[c], table.getColumnHeader(c));
			for (int r = 0; r < rowHeaders.length; r++) {
				assertEquals(content[r][c], table.get(c, r));
			}
		}
		for (int r = 0; r < rowHeaders.length; r++) {
			assertEquals(rowHeaders[r], table.getRowHeader(r));
		}
	}

	private Table openTable(final String tableSource,
		final IOPlugin<Table> tableIO) throws IOException
	{
		final DataHandleService dataHandleService = ctx.service(DataHandleService.class);
		Table result;
		File tempFile = File.createTempFile("openTest", ".txt");
		tempFiles.add(tempFile);
		try (DataHandle<Location> destHandle = dataHandleService.create(new FileLocation(tempFile))) {
			destHandle.write(tableSource.getBytes());
			result = tableIO.open(tempFile.getAbsolutePath());
		}
		return result;
	}

	private String saveTable(final Table table,
		final IOPlugin<Table> tableIO) throws IOException
	{
		final DataHandleService dataHandleService = ctx.service(DataHandleService.class);
		String result;
		File tempFile = File.createTempFile("saveTest", ".txt");
		tempFiles.add(tempFile);
		try (DataHandle<Location> sourceHandle = dataHandleService.create(new FileLocation(tempFile))) {
			tableIO.save(table, tempFile.getAbsolutePath());
			result = sourceHandle.readString(Integer.MAX_VALUE);
		}
		return result;
	}

	private void setValues(final Object instance, final String[] fieldNames,
		final Object[] values) throws SecurityException
	{
		final Class<?> cls = instance.getClass();
		final List<Field> fields = ClassUtils.getAnnotatedFields(cls,
			Parameter.class);
		final HashMap<String, Field> fieldMap = new HashMap<>();
		for (final Field field : fields) {
			fieldMap.put(field.getName(), field);
		}
		for (int i = 0; i < fieldNames.length; i++) {
			ClassUtils.setValue(fieldMap.get(fieldNames[i]), instance, values[i]);
		}
	}

	private String makeTableSource(final String[][] cells, final String separator,
		final String eol)
	{
		final StringBuilder table = new StringBuilder();
		for (final String[] row : cells) {
			table.append(String.join(separator, row)).append(eol);
		}
		return table.toString();
	}
}
