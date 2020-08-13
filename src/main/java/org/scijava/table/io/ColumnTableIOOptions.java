/*
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

import org.scijava.optional.AbstractOptions;

import java.util.function.Function;

/**
 * @author Deborah Schmidt
 */
public class ColumnTableIOOptions extends AbstractOptions<ColumnTableIOOptions> {

	public final ColumnTableIOOptions.Values values = new ColumnTableIOOptions.Values();
	private static final String parserKey = "parser";
	private static final String formatterKey = "formatter";

	ColumnTableIOOptions formatter(Function<Object, String> formatter) {
		return setValue(formatterKey, formatter);
	}

	ColumnTableIOOptions parser(Function<String, ?> parser) {
		return setValue(parserKey, parser);
	}

	public class Values extends AbstractValues {

		public Function<Object, String> formatter() {
			return getValueOrDefault(formatterKey, String::valueOf);
		}

		public Function<String, ?> parser() {
			return getValueOrDefault(parserKey, String::valueOf);
		}
	}

}
