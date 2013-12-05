package net.sf.relish;

import static net.sf.relish.TableMatcher.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TableMatcherTest {

	@Test
	public void testNewMatcher_EQUAL() {

		assertTrue(EQUAL.newMatcher(null).matches(null));
		assertTrue(EQUAL.newMatcher(Arrays.asList(new Integer[] {})).matches(Arrays.asList(new Integer[] {})));
		assertFalse(EQUAL.newMatcher(Arrays.asList(new Integer[] { 1, 2 })).matches(Arrays.asList(new Integer[] { 1, 2, 3 })));
		assertTrue(EQUAL.newMatcher(Arrays.asList(new Integer[] { 1, 2, 3 })).matches(Arrays.asList(new Integer[] { 1, 2, 3 })));
		assertFalse(EQUAL.newMatcher(Arrays.asList(new Integer[] { 1, 2, 3 })).matches(Arrays.asList(new Integer[] { 1, 3, 2 })));
		assertFalse(EQUAL.newMatcher(Arrays.asList(new Integer[] { 1, 2, 3, 4 })).matches(Arrays.asList(new Integer[] { 1, 3, 2 })));
	}

	@Test
	public void testNewMatcher_BE() {

		assertTrue(BE.newMatcher(null).matches(null));
		assertTrue(BE.newMatcher(Arrays.asList(new Integer[] {})).matches(Arrays.asList(new Integer[] {})));
		assertFalse(BE.newMatcher(Arrays.asList(new Integer[] { 1, 2 })).matches(Arrays.asList(new Integer[] { 1, 2, 3 })));
		assertTrue(BE.newMatcher(Arrays.asList(new Integer[] { 1, 2, 3 })).matches(Arrays.asList(new Integer[] { 1, 2, 3 })));
		assertTrue(BE.newMatcher(Arrays.asList(new Integer[] { 1, 2, 3 })).matches(Arrays.asList(new Integer[] { 1, 3, 2 })));
		assertFalse(EQUAL.newMatcher(Arrays.asList(new Integer[] { 1, 2, 3, 4 })).matches(Arrays.asList(new Integer[] { 1, 3, 2 })));
	}

	@Test
	public void testNewMatcher_INCLUDE() {

		assertTrue(INCLUDE.newMatcher(null).matches(null));
		assertTrue(INCLUDE.newMatcher(Arrays.asList(new Integer[] {})).matches(Arrays.asList(new Integer[] {})));
		assertTrue(INCLUDE.newMatcher(Arrays.asList(new Integer[] { 1, 2 })).matches(Arrays.asList(new Integer[] { 1, 2, 3 })));
		assertTrue(INCLUDE.newMatcher(Arrays.asList(new Integer[] { 1, 2, 3 })).matches(Arrays.asList(new Integer[] { 1, 2, 3 })));
		assertTrue(INCLUDE.newMatcher(Arrays.asList(new Integer[] { 1, 2, 3 })).matches(Arrays.asList(new Integer[] { 1, 3, 2 })));
		assertFalse(EQUAL.newMatcher(Arrays.asList(new Integer[] { 1, 2, 3, 4 })).matches(Arrays.asList(new Integer[] { 1, 3, 2 })));
	}
}
