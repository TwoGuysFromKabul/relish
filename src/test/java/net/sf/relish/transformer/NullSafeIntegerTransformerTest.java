package net.sf.relish.transformer;

import static org.junit.Assert.*;

import org.junit.Test;

public class NullSafeIntegerTransformerTest {

	@Test
	public void testTransform_NotNull() {
		assertEquals(Integer.valueOf(123), new NullSafeIntegerTransformer().transform("123"));
	}

	@Test
	public void testTransform_Null() {
		assertEquals(Integer.valueOf(0), new NullSafeIntegerTransformer().transform(null));
	}
}
