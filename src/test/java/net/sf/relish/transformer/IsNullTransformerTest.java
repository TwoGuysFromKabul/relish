package net.sf.relish.transformer;

import static org.junit.Assert.*;

import org.junit.Test;

public class IsNullTransformerTest {

	@Test
	public void testTransform_NotNull() {
		assertFalse(new IsNullTransformer().transform("abc"));
	}

	@Test
	public void testTransform_Null() {
		assertTrue(new IsNullTransformer().transform(null));
	}
}
