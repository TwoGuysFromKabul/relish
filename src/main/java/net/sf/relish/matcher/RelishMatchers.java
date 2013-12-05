package net.sf.relish.matcher;

import java.util.Collection;

import org.hamcrest.Matcher;

/**
 * {@link Matcher}s for use with JUnit and Mockito. You should try to use existing matchers in <code>org.hamcrest.CoreMatchers</code> or
 * <code>org.junit.matchers.JUnitMatchers</code> before adding new ones here.
 */
public final class RelishMatchers {

	/**
	 * Creates a matcher for collections that matches when the examined Collection contains all the elements from the specified Collection:
	 * <code>assertThat(Arrays.asList("abc", "def", contains(Arrays.asList("abc")))</code>
	 */
	public static <T> Matcher<Collection<T>> contains(Collection<T> expected) {
		return Contains.contains(expected);
	}

	/**
	 * Creates a matcher that matches when the examined Collection contains all the elements from the specified Collection and both Collections are the same
	 * size.
	 */
	public static <T> Matcher<Collection<T>> containsExactly(Collection<T> expected) {
		return ContainsExactly.containsExactly(expected);
	}

	/**
	 * Creates a matcher that matches when the examined string matches the specified regular expression
	 */
	public static Matcher<String> matches(String regex) {
		return Matches.matches(regex);
	}

	/**
	 * Creates a matcher that matches when the examined {@link Comparable} is greater than the expected comparable:
	 * <code>examined.compareTo(expected) > 0</code>
	 */
	public static <T extends Comparable<T>> Matcher<T> greaterThan(T expected) {
		return GreaterThan.greaterThan(expected);
	}

	/**
	 * Creates a matcher that matches when the examined {@link Comparable} is less than the expected comparable: <code>examined.compareTo(expected) < 0</code>
	 */
	public static <T extends Comparable<T>> Matcher<T> lessThan(T expected) {
		return LessThan.lessThan(expected);
	}

	/**
	 * Creates a matcher that matches when the examined {@link Comparable} is greater than the expected comparable:
	 * <code>examined.compareTo(expected) > 0</code>
	 */
	public static <T extends Comparable<T>> Matcher<T> gt(T expected) {
		return GreaterThan.greaterThan(expected);
	}

	/**
	 * Creates a matcher that matches when the examined {@link Comparable} is less than the expected comparable: <code>examined.compareTo(expected) < 0</code>
	 */
	public static <T extends Comparable<T>> Matcher<T> lt(T expected) {
		return LessThan.lessThan(expected);
	}

	/**
	 * Creates a matcher that matches when the examined {@link Comparable} is greater than or equal to the expected comparable:
	 * <code>examined.compareTo(expected) > 0</code>
	 */
	public static <T extends Comparable<T>> Matcher<T> gte(T expected) {
		return GreaterThanOrEqualTo.greaterThanOrEqualTo(expected);
	}

	/**
	 * Creates a matcher that matches when the examined {@link Comparable} is less than or equal to the expected comparable:
	 * <code>examined.compareTo(expected) < 0</code>
	 */
	public static <T extends Comparable<T>> Matcher<T> lte(T expected) {
		return LessThanOrEqualTo.lessThanOrEqualTo(expected);
	}

	private RelishMatchers() {
	}
}
