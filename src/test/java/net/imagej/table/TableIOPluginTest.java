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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests {@link TableIOPlugin}.
 * 
 * @author Stefan Helfrich
 */
public class TableIOPluginTest {

	private static TableIOPlugin tableIOPlugin;

	private static File testCsv;

	@ClassRule
	public static TemporaryFolder folder = new TemporaryFolder();

	@BeforeClass
	public static void setUp() throws IOException {
		tableIOPlugin = new TableIOPlugin();

		testCsv = folder.newFile("testTableIO.csv");
	}

	@Test
	public void testSupportsOpen() {
		assertTrue(tableIOPlugin.supportsOpen(testCsv.toString()));
	}

	@Test
	public void testOpen() throws IOException {
		fillTestCsvWithContent();

		tableIOPlugin.open(testCsv.toString());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testSave() throws IOException {
		Table table = new DefaultGenericTable();
		table.appendColumn("LabelID");
		table.appendColumn("Size");

		table.appendRows(2);

		table.set("LabelID", 0, 1);
		table.set("Size", 0, 10);

		table.set("LabelID", 1, 2);
		table.set("Size", 1, 20);

		tableIOPlugin.save(table, testCsv.toString());
	}

	@Test
	public void testSupportsSave() {
		assertTrue(tableIOPlugin.supportsSave(testCsv.toString()));
	}

	/**
	 * @throws FileNotFoundException
	 */
	private void fillTestCsvWithContent() throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(testCsv)) {
			out.print("LabelID\tSize\r\n");
			out.print("1\t10\r\n");
			out.print("2\t20\r\n");
			out.flush();
		}
	}

}
