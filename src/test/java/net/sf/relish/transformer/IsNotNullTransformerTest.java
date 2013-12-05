package net.sf.relish.transformer;

import static org.junit.Assert.*;

import org.junit.Test;

public class IsNotNullTransformerTest {

	@Test
	public void testTransform_NotNull() {
		assertTrue(new IsNotNullTransformer().transform("abc"));
	}

	@Test
	public void testTransform_Null() {
		assertFalse(new IsNotNullTransformer().transform(null));
	}
}
