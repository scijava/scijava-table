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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.scijava.Priority;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.table.DefaultGenericTable;
import org.scijava.table.GenericTable;
import org.scijava.table.Table;
import org.scijava.util.FileUtils;

/**
 * Plugin for reading/writing {@link Table}s.
 *
 * @author Leon Yang
 */
@SuppressWarnings("rawtypes")
@Plugin(type = TableIOPlugin.class, priority = Priority.LOW)
public class DefaultTableIOPlugin extends AbstractIOPlugin<Table> implements
	TableIOPlugin
{

	@Parameter
	private DataHandleService dataHandleService;

	// FIXME: The "txt" extension is extremely general and will conflict with
	// other plugins. Consider another way to check supportsOpen/Close.
	private static final Set<String> SUPPORTED_EXTENSIONS = //
		Collections.unmodifiableSet(new HashSet<>(//
			Arrays.asList("csv", "txt", "prn", "dif", "rtf")));

	@Override
	public boolean supportsOpen(final Location source) {
		if (!(source instanceof FileLocation)) return false;
		return supportsOpen(((FileLocation) source).getFile());
	}

	@Override
	public boolean supportsOpen(final String source) {
		return supportsOpen(new File(source));
	}

	@Override
	public boolean supportsSave(final Object data, final String destination) {
		return supports(destination) && //
			Table.class.isAssignableFrom(data.getClass());
	}

	@Override
	public boolean supportsSave(final Location source) {
		return supportsOpen(source);
	}

	@Override
	public boolean supportsSave(final String source) {
		return supportsOpen(source);
	}

	/**
	 * Process a given line into a list of tokens.
	 */
	private ArrayList<String> processRow(final String line, final char separator,
		final char quote) throws IOException
	{
		final ArrayList<String> row = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		int idx = 0;
		int start = idx;
		while (idx < line.length()) {
			if (line.charAt(idx) == quote) {
				sb.append(line, start, idx);
				boolean quoted = true;
				idx++;
				start = idx;
				// find quoted string
				while (idx < line.length()) {
					if (line.charAt(idx) == quote) {
						sb.append(line, start, idx);
						if (idx + 1 < line.length() && line.charAt(idx + 1) == quote) {
							sb.append(quote);
							idx += 2;
							start = idx;
						}
						else {
							idx++;
							start = idx;
							quoted = false;
							break;
						}
					}
					else {
						idx++;
					}
				}
				if (quoted) {
					throw new IOException(String.format(
						"Unbalanced quote at position %d: %s", idx, line));
				}
			}
			else if (line.charAt(idx) == separator) {
				sb.append(line, start, idx);
				row.add(sb.toString());
				sb.setLength(0);
				idx++;
				start = idx;
			}
			else {
				idx++;
			}
		}
		sb.append(line, start, idx);
		row.add(sb.toString());
		return row;
	}

	@Override
	public GenericTable open(final Location source, final TableIOOptions options)
		throws IOException
	{
		return open(source, options.values);
	}

	private GenericTable open(final Location source,
		final TableIOOptions.Values options) throws IOException
	{

		final GenericTable table = new DefaultGenericTable();

		try (final DataHandle<? extends Location> handle = //
			dataHandleService.create(source))
		{
			if (!handle.exists()) {
				throw new IOException("Cannot open source");
			}
			final long length = handle.length();

			final byte[] buffer = new byte[(int) length];
			handle.read(buffer);

			final String text = new String(buffer);

			final char separator = options.columnDelimiter();
			final char quote = options.quote();
			final boolean readRowHeaders = options.readRowHeaders();
			final boolean readColHeaders = options.readColumnHeaders();

			// split by any line delimiter
			final String[] lines = text.split("\\R");
			if (lines.length == 0) return table;
			// process first line to get number of cols
			final Map<Integer, Function<String, ?>> columnParsers = new HashMap<>();
			{
				final ArrayList<String> tokens = processRow(lines[0], separator, quote);
				if (readColHeaders) {
					final List<String> colHeaders;
					if (readRowHeaders) colHeaders = tokens.subList(1, tokens.size());
					else colHeaders = tokens;
					final String[] colHeadersArr = new String[colHeaders.size()];
					table.appendColumns(colHeaders.toArray(colHeadersArr));
				}
				else {
					final List<String> cols;
					if (readRowHeaders) {
						cols = tokens.subList(1, tokens.size());
						table.appendColumns(cols.size());
						table.appendRow(tokens.get(0));
					}
					else {
						cols = tokens;
						table.appendColumns(cols.size());
						table.appendRow();
					}
					for (int i = 0; i < cols.size(); i++) {
						final Function<String, ?> parser = getParser(cols.get(i), i,
							options);
						columnParsers.put(i, parser);
						table.set(i, 0, parser.apply(cols.get(i)));
					}
				}
			}
			for (int lineNum = 1; lineNum < lines.length; lineNum++) {
				final String line = lines[lineNum];
				final ArrayList<String> tokens = processRow(line, separator, quote);
				final List<String> cols;
				if (readRowHeaders) {
					cols = tokens.subList(1, tokens.size());
					table.appendRow(tokens.get(0));
				}
				else {
					cols = tokens;
					table.appendRow();
				}
				if (cols.size() != table.getColumnCount()) {
					throw new IOException("Line " + table.getRowCount() +
						" is not the same length as the first line.");
				}
				for (int i = 0; i < cols.size(); i++) {
					if (lineNum == 1 && readColHeaders) {
						columnParsers.put(i, getParser(cols.get(i), i, options));
					}
					table.set(i, lineNum - 1, columnParsers.get(i).apply(cols.get(i)));
				}
			}
		}
		return table;
	}

	private static Function<String, ?> getParser(final String content,
		final int column, final TableIOOptions.Values options)
	{
		final ColumnTableIOOptions.Values colOptions = options.column(column);
		if (colOptions != null) return colOptions.parser();
		if (options.guessParser()) return guessParser(content);
		return options.parser();
	}

	static Function<String, ?> guessParser(final String content) {
		try {
			final Function<String, ?> function = s -> Double.valueOf(s.replace(
				"infinity", "Infinity").replace("Nan", "NaN"));
			function.apply(content);
			return function;
		}
		catch (final NumberFormatException ignored) {}
		if (content.equalsIgnoreCase("true") || content.equalsIgnoreCase("false")) {
			return Boolean::valueOf;
		}
		return String::valueOf;
	}

	@Override
	public void save(final Table table, final Location destination,
		final TableIOOptions options) throws IOException
	{
		save(table, destination, options.values);
	}

	private boolean supportsOpen(final File file) {
		if (!file.exists() || file.isDirectory()) return false;
		final String ext = FileUtils.getExtension(file).toLowerCase();
		return SUPPORTED_EXTENSIONS.contains(ext);
	}

	private void save(final Table table, final Location destination,
		final TableIOOptions.Values options) throws IOException
	{

		try (final DataHandle<Location> handle = //
			dataHandleService.create(destination))
		{
			final boolean writeRH = options.writeRowHeaders();
			final boolean writeCH = options.writeColumnHeaders();
			final char separator = options.columnDelimiter();
			final String eol = options.rowDelimiter();
			final char quote = options.quote();

			final StringBuilder sb = new StringBuilder();
			// write column headers
			if (writeCH) {
				if (writeRH) {
					sb.append(tryQuote(options.cornerText(), separator, quote));
					if (table.getColumnCount() > 0) {
						sb.append(separator);
						sb.append(tryQuote(table.getColumnHeader(0), separator, quote));
					}
				}
				// avoid adding extra separator when there is 0 column
				else if (table.getColumnCount() > 0) {
					sb.append(tryQuote(table.getColumnHeader(0), separator, quote));
				}
				for (int col = 1; col < table.getColumnCount(); col++) {
					sb.append(separator);
					sb.append(tryQuote(table.getColumnHeader(col), separator, quote));
				}
				sb.append(eol);
				handle.writeBytes(sb.toString());
				sb.setLength(0);
			}
			// write each row
			for (int row = 0; row < table.getRowCount(); row++) {
				Function<Object, String> formatter = getFormatter(options, 0);
				if (writeRH) {
					sb.append(tryQuote(table.getRowHeader(row), separator, quote));
					if (table.getColumnCount() > 0) {
						sb.append(separator);
						sb.append(tryQuote(formatter.apply(table.get(0, row)), separator,
							quote));
					}
				}
				// avoid adding extra separator when there is 0 column
				else if (table.getColumnCount() > 0) {
					sb.append(tryQuote(formatter.apply(table.get(0, row)), separator,
						quote));
				}
				for (int col = 1; col < table.getColumnCount(); col++) {
					formatter = getFormatter(options, col);
					sb.append(separator);
					sb.append(tryQuote(formatter.apply(table.get(col, row)), separator,
						quote));
				}
				sb.append(eol);
				handle.writeBytes(sb.toString());
				sb.setLength(0);
			}
		}

	}

	private Function<Object, String> getFormatter(
		final TableIOOptions.Values options, final int i)
	{
		final ColumnTableIOOptions.Values columnOptions = options.column(i);
		if (columnOptions != null) return columnOptions.formatter();
		return options.formatter();
	}

	/**
	 * Try to quote a string if:
	 * <li>it is null or empty</li>
	 * <li>it has quotes inside</li>
	 * <li>it has separators or EOL inside</li>
	 *
	 * @param str string to quote
	 * @return string, possibly quoted
	 */
	private String tryQuote(final String str, final char separator,
		final char quote)
	{
		if (str == null || str.length() == 0) return "" + quote + quote;
		if (str.indexOf(quote) != -1) return quote + str.replace("" + quote, "" +
			quote + quote) + quote;
		if (str.indexOf(separator) != -1) return quote + str + quote;
		return str;
	}
}
