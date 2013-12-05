package net.sf.relish.matcher;

import static net.sf.relish.matcher.LessThan.*;
import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.junit.Test;

public class LessThanTest {

	Matcher<Integer> matcher = lessThan(2);

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
		assertFalse(matcher.matches(2));
	}
}
