package net.sf.relish;

import static net.sf.relish.CountQuantifier.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.Callable;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountQuantifierTest {

	@Mock Callable<Integer> callable;

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(callable.call()).thenReturn(1).thenReturn(2).thenReturn(3).thenReturn(4).thenReturn(5);
	}

	@Test
	public void testNewMatcher_AtLeast() throws Exception {

		Matcher<Integer> matcher = AT_LEAST.newMatcher(123);
		assertFalse(matcher.matches(122));
		assertTrue(matcher.matches(123));
		assertTrue(matcher.matches(124));
	}

	@Test
	public void testNewMatcher_AtMost() throws Exception {
		Matcher<Integer> matcher = AT_MOST.newMatcher(123);
		assertTrue(matcher.matches(122));
		assertTrue(matcher.matches(123));
		assertFalse(matcher.matches(124));
	}

	@Test
	public void testNewMatcher_AtExactly() throws Exception {
		Matcher<Integer> matcher = EXACTLY.newMatcher(123);
		assertFalse(matcher.matches(122));
		assertTrue(matcher.matches(123));
		assertFalse(matcher.matches(124));
	}
}
