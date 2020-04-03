package org.scijava.table.io;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TableIOOptionsTest {

	@Test
	public void testTypeOption() {
		TableIOOptions options = new TableIOOptions();
		assertTrue(options.values.guessParser());
		options.type(Double.class);
		assertFalse(options.values.guessParser());
		assertEquals(Double.POSITIVE_INFINITY, options.values.parser().apply("Infinity"));
	}

	@Test
	public void testColumnTypeOption() {
		TableIOOptions options = new TableIOOptions();
		assertNull(options.values.column(0));
		options.columnType(0, Double.class);
		assertNotNull(options.values.column(0));
		assertEquals(Double.POSITIVE_INFINITY, options.values.column(0).parser().apply("Infinity"));
	}
}
