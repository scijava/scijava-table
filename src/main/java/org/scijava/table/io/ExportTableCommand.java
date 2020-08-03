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

package org.scijava.table.io;

import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.log.LogService;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.table.TableDisplay;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;

@Plugin(type = Command.class, menu = { @Menu(label = MenuConstants.FILE_LABEL,
	weight = MenuConstants.FILE_WEIGHT, mnemonic = MenuConstants.FILE_MNEMONIC),
	@Menu(label = "Export"), @Menu(label = "Table...  ") })
public class ExportTableCommand extends ContextCommand {

	@Parameter(required = false, label = "Write column headers")
	private boolean writeColHeaders = true;

	@Parameter(required = false, label = "Write row headers")
	private boolean writeRowHeaders = true;

	@Parameter(required = false, label = "Column delimiter")
	private char columnDelimiter = ',';

	@Parameter
	private LogService log;

	@Parameter
	private TableDisplay tableDisplay;

	@Parameter
	private TableIOService tableIO;

	@Parameter
	private UIService uiService;

	@Parameter(label = "File to save", style = FileWidget.SAVE_STYLE,
		persist = false)
	private File outputFile;

	@Override
	public void run() {
		try {
			TableIOOptions options = new TableIOOptions()
					.writeColumnHeaders(writeColHeaders)
					.writeRowHeaders(writeRowHeaders)
					.columnDelimiter(columnDelimiter);
			tableIO.save(tableDisplay.get(0), outputFile.getAbsolutePath(), options);
		}
		catch (IOException exc) {
			log.error(exc);
			uiService.showDialog(exc.getMessage(), "Error Saving Table");
			return;
		}
	}
}
