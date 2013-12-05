package net.sf.relish.matcher;

import static net.sf.relish.matcher.GreaterThan.*;
import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.junit.Test;

public class GreaterThanTest {

	Matcher<Integer> matcher = greaterThan(2);

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
		assertFalse(matcher.matches(2));
	}
}
