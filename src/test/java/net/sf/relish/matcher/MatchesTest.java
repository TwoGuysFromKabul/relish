package net.sf.relish.matcher;

import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.junit.Test;

public class MatchesTest {

	Matcher<String> matcher = Matches.matches("[abc]+");

	@Test
	public void testMatches_Match() {

		assertTrue(matcher.matches("a"));
		assertTrue(matcher.matches("abc"));
		assertTrue(matcher.matches("aaaa"));
	}

	@Test
	public void testMatches_NoMatch() {
		assertFalse(matcher.matches(""));
		assertFalse(matcher.matches("ax"));
	}
}
