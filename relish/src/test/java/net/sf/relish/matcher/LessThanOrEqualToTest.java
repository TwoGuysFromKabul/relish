package net.sf.relish.matcher;

import static net.sf.relish.matcher.LessThanOrEqualTo.*;
import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.junit.Test;

public class LessThanOrEqualToTest {

	Matcher<Integer> matcher = lessThanOrEqualTo(2);

	@Test
	public void testMatches_Match() {
		assertTrue(matcher.matches(1));
	}

	@Test
	public void testMatches_IsGreaterThan() {
		assertFalse(matcher.matches(3));
	}

	@Test
	public void testMatches_IsEqualTo() {
		assertTrue(matcher.matches(2));
	}
}
