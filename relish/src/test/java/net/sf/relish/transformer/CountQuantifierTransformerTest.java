package net.sf.relish.transformer;

import static net.sf.relish.CountQuantifier.*;
import static org.junit.Assert.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sf.relish.transformer.CountQuantifierTransformer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountQuantifierTransformerTest {

	CountQuantifierTransformer transformer = new CountQuantifierTransformer();

	@Test
	public void testTransform_AtLeast() {

		assertEquals(AT_LEAST, transformer.transform("at least"));
	}

	@Test
	public void testTransform_AtMost() {
		assertEquals(AT_MOST, transformer.transform("at most"));
	}

	@Test
	public void testTransform_Exactly() {
		assertEquals(EXACTLY, transformer.transform("exactly"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTransform_UnknownValue() {
		transformer.transform("foo");
	}

}
