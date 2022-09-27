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

import org.scijava.optional.AbstractOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Deborah Schmidt
 */
public class TableIOOptions extends AbstractOptions<TableIOOptions> {

	public final Values values = new Values();
	private final Map<Integer, ColumnTableIOOptions> columnOptions;
	private static final String readColHeadersKey = "readColHeaders";
	private static final String columnOptionsKey = "columnOptions";
	private static final String writeColHeadersKey = "writeColHeaders";
	private static final String readRowHeadersKey = "readRowHeaders";
	private static final String writeRowHeadersKey = "writeRowHeaders";
	private static final String columnDelimiterKey = "columnDelimiter";
	private static final String rowDelimiterKey = "rowDelimiter";
	private static final String quoteKey = "quote";
	private static final String cornerTextKey = "cornerText";
	private static final String guessParserKey = "guessParser";
	private static final String parserKey = "parser";
	private static final String formatterKey = "formatter";

	public TableIOOptions() {
		this.columnOptions = new HashMap<>();
		setValue(columnOptionsKey, columnOptions);
	}

	/**
	 * @return Default {@link TableIOOptions} instance
	 */
	public static TableIOOptions options()
	{
		return new TableIOOptions();
	}

	/**
	 * @param readColHeaders Whether to read the first column of the input file as column headers.
	 */
	public TableIOOptions readColumnHeaders(boolean readColHeaders) {
		return setValue(readColHeadersKey, readColHeaders);
	}

	/**
	 * @param writeColHeaders Whether to write column headers to file.
	 */
	public TableIOOptions writeColumnHeaders(boolean writeColHeaders) {
		return setValue(writeColHeadersKey, writeColHeaders);
	}

	/**
	 * @param readRowHeaders Whether to read the first column of the input file as row headers.
	 */
	public TableIOOptions readRowHeaders(boolean readRowHeaders) {
		return setValue(readRowHeadersKey, readRowHeaders);
	}

	/**
	 * @param writeRowHeaders Whether to write row headers to file.
	 */
	public TableIOOptions writeRowHeaders(boolean writeRowHeaders) {
		return setValue(writeRowHeadersKey, writeRowHeaders);
	}

	/**
	 * @param columnDelimiter Character separating cells in each row of the table.
	 */
	public TableIOOptions columnDelimiter(char columnDelimiter) {
		return setValue(columnDelimiterKey, columnDelimiter);
	}

	/**
	 * @param rowDelimiter Delimiter used at end of row when writing to or reading from file.
	 */
	public TableIOOptions rowDelimiter(String rowDelimiter) {
		return setValue(rowDelimiterKey, rowDelimiter);
	}

	/**
	 * @param quote Character used for escaping separator and empty strings.
	 */
	public TableIOOptions quote(char quote) {
		return setValue(quoteKey, quote);
	}

	/**
	 * @param cornerText Text that appears at the top left corner when both column and row headers present.
	 */
	public TableIOOptions cornerText(String cornerText) {
		return setValue(cornerTextKey, cornerText);
	}

	/**
	 * @param guessParser If true, allow opener to guess the data type of each column.
	 */
	public TableIOOptions guessParser(boolean guessParser) {
		return setValue(guessParserKey, guessParser);
	}

	/**
	 * @param parser Parser to use when converting table entries into data objects.
	 */
	public TableIOOptions parser(Function<String, ?> parser) {
		guessParser(false);
		return setValue(parserKey, parser);
	}

	/**
	 * @param formatter Formatter to use when writing data objects into table entries.
	 */
	public TableIOOptions formatter(Function<Object, String> formatter) {
		return setValue(formatterKey, formatter);
	}

	/**
	 * Alternatifely to guessing the data type or specifying a parser, specify the desired data type of all table entries
	 * @param type the data type of all table entries
	 */
	public TableIOOptions type(Class<?> type) {
		return parser(getParser(type));
	}

	/**
	 * In case each data type vary between columns, this method can be used to set the data type of a specific column.
	 * @param column the index of the column
	 * @param type the data type of the column
	 */
	public TableIOOptions columnType(int column, Class<?> type) {
		ColumnTableIOOptions options = new ColumnTableIOOptions();
		columnOptions.putIfAbsent(column, options);
		options.formatter(String::valueOf).parser(getParser(type));
		return setValue(columnOptionsKey, columnOptions);
	}

	/**
	 * In case each data type vary between columns, this method can be used to set the parser for a specific column.
	 * @param column the index of the column
	 * @param parser the parser used to convert entries of this column into data objects
	 */
	public TableIOOptions columnParser(int column, Function<String, Object> parser) {
		columnOptions.putIfAbsent(column, new ColumnTableIOOptions());
		columnOptions.get(column).parser(parser);
		return this;
	}

	/**
	 * @param column the index of the column
	 * @param formatter the formatter used to convert data objects into entries of this column
	 */
	public TableIOOptions columnFormatter(int column, Function<Object, String> formatter) {
		columnOptions.putIfAbsent(column, new ColumnTableIOOptions());
		columnOptions.get(column).formatter(formatter);
		return this;
	}

	private Function<String, ?> getParser(Class<?> type) {
		if(type.equals(String.class)) return String::valueOf;
		if(type.equals(Double.class)) return Double::valueOf;
		if(type.equals(Float.class)) return Float::valueOf;
		if(type.equals(Integer.class)) return Integer::valueOf;
		if(type.equals(Long.class)) return Long::valueOf;
		if(type.equals(Boolean.class)) return Boolean::valueOf;
		return values.parser();
	}

	public class Values extends AbstractValues
	{
		/**
		 * @return Whether to read the first column of the input file as row headers.
		 */
		public boolean readColumnHeaders() {
			return getValueOrDefault(readColHeadersKey, true);
		}

		/**
		 * @return Whether to write column headers to file.
		 */
		public boolean writeColumnHeaders() {
			return getValueOrDefault(writeColHeadersKey, true);
		}

		/**
		 * @return Whether to read the first column of the input file as row headers.
		 */
		public boolean readRowHeaders() {
			return getValueOrDefault(readRowHeadersKey, true);
		}

		/**
		 * @return Whether to write row headers to file.
		 */
		public boolean writeRowHeaders() {
			return getValueOrDefault(writeRowHeadersKey, true);
		}

		/**
		 * @return Character separating cells in each row of the table.
		 */
		public char columnDelimiter() {
			return getValueOrDefault(columnDelimiterKey, ',');
		}

		/**
		 * @return Delimiter used at end of row when writing to or reading from file.
		 */
		public String rowDelimiter() {
			return getValueOrDefault(rowDelimiterKey, System.lineSeparator());
		}

		/**
		 * @return Character used for escaping separator and empty strings.
		 */
		public char quote() {
			return getValueOrDefault(quoteKey, '"');
		}

		/**
		 * @return Text that appears at the top left corner when both column and row headers present.
		 */
		public String cornerText() {
			return getValueOrDefault(cornerTextKey, "\\");
		}

		/**
		 * @return If true, allow opener to guess the data type of each column.
		 */
		public boolean guessParser() {
			return getValueOrDefault(guessParserKey, true);
		}

		/**
		 * @return Parser to use when converting table entries into data objects.
		 */
		public Function<String, ?> parser() {
			return getValueOrDefault(parserKey, String::valueOf);
		}

		/**
		 * @return Formatter to use when writing data objects into table entries.
		 */
		public Function<Object, String> formatter() {
			return getValueOrDefault(formatterKey, String::valueOf);
		}

		/**
		 * @param column the index of the column
		 * @return the values of this column's options
		 */
		public ColumnTableIOOptions.Values column(int column) {
			ColumnTableIOOptions columnOptions = getValueOrDefault(columnOptionsKey, new HashMap<Integer, ColumnTableIOOptions>()).get(column);
			if(columnOptions == null) return null;
			return columnOptions.values;
		}
	}
}
