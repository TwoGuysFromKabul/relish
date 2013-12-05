package net.sf.relish.matcher;

import static net.sf.relish.matcher.RelishMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class RelishMatchersTest {

	@Test
	public void testContains_ContainsAll() {
		assertThat(Arrays.asList(new String[] { "abc", "def" }), contains(Arrays.asList(new String[] { "abc", "def" })));
	}

	@Test
	public void testContains_ContainsSome() {
		assertThat(Arrays.asList(new String[] { "abc", "def" }), contains(Arrays.asList(new String[] { "abc" })));
	}

	@Test(expected = AssertionError.class)
	public void testContains_ContainsAllPlusMore() {
		assertThat(Arrays.asList(new String[] { "abc", "def" }), contains(Arrays.asList(new String[] { "abc", "def", "ghi" })));
	}

	@Test(expected = AssertionError.class)
	public void testContains_ContainsNone() {
		assertThat(Arrays.asList(new String[] { "abc", "def" }), contains(Arrays.asList(new String[] { "ghi" })));
	}

	@Test
	public void testContainsExactly_ContainsExactly() {
		assertThat(Arrays.asList(new String[] { "abc", "def" }), containsExactly(Arrays.asList(new String[] { "abc", "def" })));
	}

	@Test(expected = AssertionError.class)
	public void testContainsExactly_ContainsSome() {
		assertThat(Arrays.asList(new String[] { "abc", "def" }), containsExactly(Arrays.asList(new String[] { "abc" })));
	}

	@Test(expected = AssertionError.class)
	public void testContainsExactly_ContainsAllPlusMore() {
		assertThat(Arrays.asList(new String[] { "abc", "def" }), containsExactly(Arrays.asList(new String[] { "abc", "def", "ghi" })));
	}

	@Test(expected = AssertionError.class)
	public void testContainsExactly_ContainsNone() {
		assertThat(Arrays.asList(new String[] { "abc", "def" }), containsExactly(Arrays.asList(new String[] { "ghi" })));
	}

	@Test
	public void testMatches_Matches() {
		assertThat("aaaabbbb", matches("[abc]+"));
	}

	@Test(expected = AssertionError.class)
	public void testMatches_DoesNotMatch() {
		assertThat("def", matches("[abc]+"));
	}

	@Test
	public void testLessThan_IsLessThan() throws Exception {

		assertThat(1, lessThan(2));
	}

	@Test(expected = AssertionError.class)
	public void testLessThan_IsEqualTo() throws Exception {

		assertThat(2, lessThan(2));
	}

	@Test(expected = AssertionError.class)
	public void testLessThan_IsGreaterThan() throws Exception {

		assertThat(3, lessThan(2));
	}

	@Test
	public void testLt_IsLessThan() throws Exception {

		assertThat(1, lt(2));
	}

	@Test(expected = AssertionError.class)
	public void testLt_IsEqualTo() throws Exception {

		assertThat(2, lt(2));
	}

	@Test(expected = AssertionError.class)
	public void testLt_IsGreaterThan() throws Exception {

		assertThat(3, lt(2));
	}

	@Test
	public void testLte_IsLessThan() throws Exception {

		assertThat(1, lte(2));
	}

	@Test
	public void testLte_IsEqualTo() throws Exception {

		assertThat(2, lte(2));
	}

	@Test(expected = AssertionError.class)
	public void testLte_IsGreaterThan() throws Exception {

		assertThat(3, lte(2));
	}

	@Test
	public void testGreaterThan_IsGreaterThan() throws Exception {

		assertThat(3, greaterThan(2));
	}

	@Test(expected = AssertionError.class)
	public void testGreaterThan_IsEqualTo() throws Exception {

		assertThat(2, greaterThan(2));
	}

	@Test(expected = AssertionError.class)
	public void testGreaterThan_IsLessThan() throws Exception {

		assertThat(1, greaterThan(2));
	}

	@Test
	public void testGt_IsGreaterThan() throws Exception {

		assertThat(3, gt(2));
	}

	@Test(expected = AssertionError.class)
	public void testGt_IsEaualTo() throws Exception {

		assertThat(2, gt(2));
	}

	@Test(expected = AssertionError.class)
	public void testGt_IsLessThan() throws Exception {

		assertThat(1, gt(2));
	}

	@Test
	public void testGte_IsGreaterThan() throws Exception {

		assertThat(3, gte(2));
	}

	@Test
	public void testGte_IsEaualTo() throws Exception {

		assertThat(2, gte(2));
	}

	@Test(expected = AssertionError.class)
	public void testGte_IsLessThan() throws Exception {

		assertThat(1, gte(2));
	}
}
