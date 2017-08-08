/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.scijava.Priority;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.IOPlugin;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.FileUtils;

/**
 * {@link IOPlugin} for writing {@link Table}s to csv.
 * 
 * @author Stefan Helfrich
 */
@SuppressWarnings("rawtypes")
@Plugin(type = IOPlugin.class, priority = Priority.LOW_PRIORITY - 1)
public class CommonsCSVTableIOPlugin extends AbstractIOPlugin<Table> {

	@Parameter
	private LogService log;

	/** Delimiter used in CSV file */
	@Parameter(required = false)
	private char delimiter = '\t';

	/** Quote character used in CSV file */
	@Parameter(required = false)
	private char quote = '"';

	/** Line separator used in CSV file */
	@Parameter(required = false)
	private String recordSeparator = "\r\n";

	// -- IOPlugin methods --

	@Override
	public Class<Table> getDataType() {
		return Table.class;
	}

	@Override
	public boolean supportsOpen(final String source) {
		File file = new File(source);
		return FileUtils.getExtension(file).equalsIgnoreCase("csv");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Table open(final String source) throws IOException {
		// Create the CSVFormat object
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader()
			.withIgnoreEmptyLines(true).withDelimiter(delimiter).withQuote(quote)
			.withRecordSeparator(recordSeparator);

		// Create a table
		Table table = new DefaultGenericTable();

		// Initialize FileReader and CSVParser in try-with-resource
		try (FileReader fileReader = new FileReader(new File(source));
				CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);)
		{
			// Read headers
			Set<String> keySet = csvFileParser.getHeaderMap().keySet();
			String[] headers = new String[keySet.size()];
			keySet.toArray(headers);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			// Create columns and rows
			table.appendColumns(headers);
			table.appendRows(csvRecords.size());

			// Read the CSV file
			int counter = 0;
			for (CSVRecord record : csvRecords) {
				for (String columnHeader : headers) {
					table.set(columnHeader, counter, record.get(columnHeader));
				}

				counter++;
			}
		}
		catch (Exception e) {
			log.error(e);
		}

		return table;
	}

	@Override
	public void save(Table data, String destination) throws IOException {
		// Create the CSVFormat object
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines(true)
			.withDelimiter(delimiter).withQuote(quote).withRecordSeparator(
				recordSeparator);

		// Initialize FileWriter and CSVPrinter in try-with-resource
		try (FileWriter fileWriter = new FileWriter(destination);
				CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat))
		{
			// Get column headers from table
			List<String> headers = new LinkedList<>();
			for (int col = 0; col < data.getColumnCount(); ++col) {
				headers.add(data.getColumnHeader(col));
			}

			// Write headers to CSV
			csvFilePrinter.printRecord(headers);

			// Write table row-by-row to CSV
			for (int row = 0; row < data.getRowCount(); row++) {
				List<Object> record = new LinkedList<>();
				for (int col = 0; col < data.getColumnCount(); ++col) {
					record.add(data.get(col, row));
				}
				csvFilePrinter.printRecord(record);
			}

			fileWriter.flush();
		}
		catch (Exception e) {
			log.error(e);
		}
	}

	@Override
	public boolean supportsSave(final String source) {
		return supportsOpen(source);
	}
}
