package net.sf.relish.transformer;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringToBooleanTransformerTest {

	StringToBooleanTransformer<Boolean> transformer = new StringToBooleanTransformer<Boolean>("good to go", "bad apples", false) {
	};

	@Test
	public void testTransform_Allowed() {
		assertTrue(transformer.transform("good to go"));
		assertTrue(transformer.transform(" good to go"));
		assertTrue(transformer.transform("good to go "));
		assertTrue(transformer.transform(" good to go "));
	}

	@Test
	public void testTransform_NotAllowed() {
		assertFalse(transformer.transform("bad apples"));
		assertFalse(transformer.transform(" bad apples"));
		assertFalse(transformer.transform("bad apples "));
		assertFalse(transformer.transform(" bad apples "));
	}

	@Test
	public void testTransform_Null_NullIsNotFalse() {
		assertNull(transformer.transform(null));
	}

	@Test
	public void testTransform_Null_NullIsFalse() {
		assertFalse(new StringToBooleanTransformer<Boolean>("good to go", "bad apples", true) {
		}.transform(null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTransform_Invalid() throws Exception {

		transformer.transform("bunch of crap");
	}
}
