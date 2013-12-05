package net.sf.relish.matcher;

import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.junit.Test;

public class AbstractTypeSafeMatcherTest {

	Matcher<Integer> matcher = new TestMatch();

	@Test
	public void testToString() throws Exception {

		assertEquals("TestMatch <123>", matcher.toString());
	}

	private static class TestMatch extends AbstractTypeSafeMatcher<Integer> {

		public TestMatch() {
			super(123);
		}

		@Override
		protected boolean matchesSafely(Integer item) {
			return false;
		}
	};
}
