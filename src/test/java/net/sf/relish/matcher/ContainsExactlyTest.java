package net.sf.relish.matcher;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Matcher;
import org.junit.Test;

public class ContainsExactlyTest {

	Matcher<Collection<String>> matcher = ContainsExactly.containsExactly(Arrays.asList(new String[] { "abc", "def" }));

	@Test
	public void testMatches_Match() {

		assertTrue(matcher.matches(Arrays.asList(new String[] { "abc", "def" })));
		assertTrue(matcher.matches(Arrays.asList(new String[] { "def", "abc" })));
	}

	@Test
	public void testMatches_SizesAreDifferent() {
		assertFalse(matcher.matches(Arrays.asList(new String[] {})));
		assertFalse(matcher.matches(Arrays.asList(new String[] { "abc" })));
		assertFalse(matcher.matches(Arrays.asList(new String[] { "abc", "def", "ghi" })));
	}

	@Test
	public void testMatches_SizesAreEqual_ItemsAreDifferent() {
		assertFalse(matcher.matches(Arrays.asList(new String[] { "abc", "ghi" })));
	}
}
