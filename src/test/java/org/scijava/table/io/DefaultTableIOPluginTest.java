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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.scijava.table.io.DefaultTableIOPlugin.guessParser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.IOService;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.table.DefaultGenericTable;
import org.scijava.table.GenericTable;
import org.scijava.table.Table;

/**
 * Tests for {@link DefaultTableIOPlugin}.
 *
 * @author Leon Yang
 * @author Curtis Rueden
 */
@SuppressWarnings("rawtypes")
public class DefaultTableIOPluginTest {

	private Context ctx;
	private DefaultTableIOPlugin tableIO;

	@Before
	public void setUp() {
		ctx = new Context(IOService.class, DataHandleService.class);
		tableIO = ctx.service(IOService.class)//
			.getInstance(DefaultTableIOPlugin.class);
	}

	@After
	public void tearDown() {
		ctx.dispose();
		ctx = null;
		tableIO = null;
	}

	/**
	 * Tests the following methods:
	 * <ul>
	 * <li>{@link DefaultTableIOPlugin#supportsOpen(String)}</li>
	 * <li>{@link DefaultTableIOPlugin#supportsSave(String)}</li>
	 * <li>{@link DefaultTableIOPlugin#supportsOpen(Location)}</li>
	 * <li>{@link DefaultTableIOPlugin#supportsSave(Location)}</li>
	 * </ul>
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSupports() throws IOException {
		// Non-existent .csv file: supports save, but not open.
		final File nonExistentCSV = new File("thisFileDoesNotExist.csv");
		assertSupports(nonExistentCSV, false, true);

		// Existing .txt file: supports both open and save.
		final File existingTXT = createTempFile("existing");
		assertSupports(existingTXT, true, true);

		// Existing directory: not table compatible.
		final File existingNonTable = new File(".");
		assertSupports(existingNonTable, false, false);

		// Non-existent TIFF file: not table compatible.
		final File nonExistentTIFF = new File("thisFileDoesNotExist.tif");
		assertSupports(nonExistentTIFF, false, false);
	}

	/**
	 * Tests if the parser works on a common comma-delimited table.
	 */
	@Test
	public void testDefaultOptions() throws IOException {
		final String[] colHeaders = { "col1", "col2", "col3", "col4", "col5" };
		final String[] rowHeaders = { "", "" };
		final Double[][] content = { { 123.0, -123.0, 123.0, 123.0, 0.0 }, { 0.0,
			1234567890.0987654321, Double.NaN, Double.NEGATIVE_INFINITY, 0.0 } };

		final String expected = "\\,col1,col2,col3,col4,col5\n" +
			"\"\",123.0,-123.0,123.0,123.0,0.0\n" +
			"\"\",0.0,1.2345678900987654E9,NaN,-Infinity,0.0\n";

		final GenericTable table = new DefaultGenericTable();
		Arrays.stream(colHeaders).forEach(table::appendColumn);
		Arrays.stream(rowHeaders).forEach(table::appendRow);
		for (int i = 0; i < content.length; i++) {
			for (int j = 0; j < content[i].length; j++) {
				table.set(j, i, content[i][j]);
			}
		}

		assertTableEquals(colHeaders, rowHeaders, content, table);

		assertEquals(expected, saveTable(table, TableIOOptions.options()));

		final Table table2 = openTable(expected, TableIOOptions.options());

		assertTableEquals(colHeaders, rowHeaders, content, table2);
	}

	/**
	 * Tests if quoting works in different scenarios.
	 */
	@Test
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

		try {
			final TableIOOptions options = TableIOOptions.options()//
				.readColumnHeaders(true)//
				.readRowHeaders(true)//
				.writeColumnHeaders(true)//
				.writeRowHeaders(true)//
				.columnDelimiter(' ')//
				.rowDelimiter("\r\n")//
				.quote('\'')//
				.cornerText("CORNER_TEXT");
			final Table table = openTable(tableSource, options);
			assertTableEquals(colHeaders, rowHeaders, content, table);

			options.columnDelimiter(',');
			assertEquals(expected, saveTable(table, options));
		}
		catch (final Exception exc) {
			throw new AssertionError(exc.getMessage(), exc);
		}
	}

	/**
	 * Tests if small tables can be opened/saved correctly.
	 */
	@Test
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

		try {
			Table table;
			String expected;
			final TableIOOptions options = new TableIOOptions()//
				.readColumnHeaders(false)//
				.writeColumnHeaders(false)//
				.readRowHeaders(true)//
				.writeRowHeaders(true)//
				.columnDelimiter(',')//
				.cornerText("CORNER TEXT")//
				.rowDelimiter("\n")//
				.quote('\'')//
				.parser(Double::valueOf)//
				.formatter(val -> String.format("%.3f", val));
			table = openTable(makeTableSource(singleRow, ",", "\n"),
				options);
			assertTableEquals(emptyHeader, singleRowHeader, content, table);
			expected = "Row Header,3.142\n";
			assertEquals(expected, saveTable(table, options));

			options.readRowHeaders(false).writeRowHeaders(false);
			table = openTable(makeTableSource(singleCell, ",", "\n"),
				options);
			assertTableEquals(emptyHeader, emptyHeader, content, table);
			expected = "3.142\n";
			assertEquals(expected, saveTable(table, options));

			options.readColumnHeaders(true).writeColumnHeaders(true);
			table = openTable(makeTableSource(singleCol, ",", "\n"),
				options);
			assertTableEquals(singleColHeader, emptyHeader, content, table);
			expected = "Col Header\n3.142\n";
			assertEquals(expected, saveTable(table, options));

			options.readRowHeaders(true);
			table = openTable(makeTableSource(onlyColHeader, ",", "\n"),
				options);
			assertTableEquals(singleColHeader, empty, emptyContent, table);
			expected = "Col Header\n";
			assertEquals(expected, saveTable(table, options));

			options.writeColumnHeaders(false).writeRowHeaders(true);
			table = openTable(makeTableSource(onlyRowHeader, ",", "\n"),
				options);
			assertTableEquals(empty, singleRowHeader, emptyContent, table);
			expected = "Row Header\n";
			assertEquals(expected, saveTable(table, options));

			options.writeColumnHeaders(true);
			table = openTable(makeTableSource(full, ",", "\n"), options);
			assertTableEquals(singleColHeader, singleRowHeader, content, table);
			expected = "CORNER TEXT,Col Header\nRow Header,3.142\n";
			assertEquals(expected, saveTable(table, options));
		}
		catch (final Exception exc) {
			exc.printStackTrace();
			fail(exc.getMessage());
		}

	}

	@Test(expected = IOException.class)
	public void testOpenNonExist() throws IOException {
		final File nonExistentCSV = new File("thisFileDoesNotExist.csv");
		assertFalse(nonExistentCSV.exists());
		tableIO.open(nonExistentCSV.getPath());
	}

	@Test
	public void testGuessParser() {
		assertEquals("test", guessParser("test").apply("test"));
		assertEquals(false, guessParser("false").apply("false"));
		assertEquals(123.0, guessParser("123.0").apply("123.0"));
		assertEquals(-123.0, guessParser("-123.0").apply("-123.0"));
		assertEquals(3.0, guessParser("3").apply("3"));
		assertEquals(36564573745634564d, //
			guessParser("36564573745634564").apply("36564573745634564"));
		assertEquals(1234567890.0987654321, //
			guessParser("1.2345678900987654E9").apply("1.2345678900987654E9"));
		assertEquals(Double.NaN, guessParser("NaN").apply("NaN"));
		assertEquals(Double.NaN, guessParser("Nan").apply("Nan"));
		assertEquals(Double.NEGATIVE_INFINITY, //
			guessParser("-Infinity").apply("-Infinity"));
		assertEquals(Double.POSITIVE_INFINITY, //
			guessParser("infinity").apply("infinity"));
		assertEquals(0.0, guessParser("0.0").apply("0.0"));
	}

	// -- helper methods --

	/**
	 * Checks if a table has the expected column/row headers and content.
	 */
	private void assertTableEquals(final String[] colHeaders,
		final String[] rowHeaders, final Object[][] content, final Table table)
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
		final TableIOOptions options) throws IOException
	{
		final DataHandleService dataHandleService = //
			ctx.service(DataHandleService.class);
		Table result;
		final File tempFile = createTempFile("openTable");
		try (final DataHandle<Location> destHandle = //
			dataHandleService.create(new FileLocation(tempFile)))
		{
			destHandle.write(tableSource.getBytes());
			result = tableIO.open(destHandle.get(), options);
		}
		return result;
	}

	private String saveTable(final Table table, final TableIOOptions options)
		throws IOException
	{
		final DataHandleService dataHandleService = //
			ctx.service(DataHandleService.class);
		String result;
		final File tempFile = createTempFile("saveTable");
		try (final DataHandle<Location> sourceHandle = //
			dataHandleService.create(new FileLocation(tempFile)))
		{
			tableIO.save(table, sourceHandle.get(), options);
			result = sourceHandle.readString(Integer.MAX_VALUE);
		}
		return result;
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

	private File createTempFile(final String prefix) throws IOException {
		final File tempFile = //
			File.createTempFile(getClass().getName() + "." + prefix, ".txt");
		tempFile.deleteOnExit();
		return tempFile;
	}

	private void assertSupports(final File file, final boolean supportsOpen,
		final boolean supportsSave)
	{
		final boolean exists = file.exists();
		final String path = file.getPath();
		final String absPath = file.getAbsolutePath();
		final FileLocation loc = new FileLocation(file);
		assertEquals(exists, file.exists());
		assertEquals(supportsOpen, tableIO.supportsOpen(path));
		assertEquals(exists, file.exists());
		assertEquals(supportsOpen, tableIO.supportsOpen(absPath));
		assertEquals(exists, file.exists());
		assertEquals(supportsOpen, tableIO.supportsOpen(loc));
		assertEquals(exists, file.exists());
		assertEquals(supportsSave, tableIO.supportsSave(path));
		assertEquals(exists, file.exists());
		assertEquals(supportsSave, tableIO.supportsSave(absPath));
		assertEquals(exists, file.exists());
		assertEquals(supportsSave, tableIO.supportsSave(loc));
		assertEquals(exists, file.exists());
	}
}
