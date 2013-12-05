package net.sf.relish.matcher;

import static net.sf.relish.matcher.GreaterThanOrEqualTo.*;
import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.junit.Test;

public class GreaterThanOrEqualToTest {

	Matcher<Integer> matcher = greaterThanOrEqualTo(2);

	@Test
	public void testMatches_Match() {
		assertTrue(matcher.matches(3));
	}

	@Test
	public void testMatches_IsLessThan() {
		assertFalse(matcher.matches(1));
	}

	@Test
	public void testMatches_IsEqualTo() {
		assertTrue(matcher.matches(2));
	}
}
