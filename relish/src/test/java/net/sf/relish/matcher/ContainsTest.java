package net.sf.relish.matcher;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Matcher;
import org.junit.Test;

public class ContainsTest {

	Matcher<Collection<String>> matcher = Contains.contains(Arrays.asList(new String[] { "abc", "def" }));

	@Test
	public void testMatches_Match() {

		assertTrue(matcher.matches(Arrays.asList(new String[] { "abc", "def" })));
		assertTrue(matcher.matches(Arrays.asList(new String[] { "abc", "def", "ghi" })));
		assertTrue(matcher.matches(Arrays.asList(new String[] { "def", "abc" })));
		assertTrue(matcher.matches(Arrays.asList(new String[] { "def", "ghi", "abc" })));
	}

	@Test
	public void testMatches_NoMatch() {
		assertFalse(matcher.matches(Arrays.asList(new String[] {})));
		assertFalse(matcher.matches(Arrays.asList(new String[] { "abc" })));
		assertFalse(matcher.matches(Arrays.asList(new String[] { "def", })));
		assertFalse(matcher.matches(Arrays.asList(new String[] { "abc", "ghi" })));
	}
}
