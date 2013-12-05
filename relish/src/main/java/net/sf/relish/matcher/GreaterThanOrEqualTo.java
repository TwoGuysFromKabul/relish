package net.sf.relish.matcher;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * A {@link Matcher} that matches when the examined {@link Comparable} is greater than or equal to the expected comparable:
 * <code>examined.compareTo(expected) >= 0</code>
 */
final class GreaterThanOrEqualTo<T extends Comparable<T>> extends AbstractTypeSafeMatcher<T> {

	private GreaterThanOrEqualTo(T expected) {
		super(expected);
	}

	/**
	 * @see org.hamcrest.TypeSafeMatcher#matchesSafely(java.lang.Object)
	 */
	@Override
	protected boolean matchesSafely(T item) {
		return item.compareTo(expected) >= 0;
	}

	/**
	 * Creates a matcher that matches when the examined {@link Comparable} is greater than or equal to the expected comparable:
	 * <code>examined.compareTo(expected) > 0</code>
	 */
	@Factory
	public static <T extends Comparable<T>> Matcher<T> greaterThanOrEqualTo(T expected) {
		return new GreaterThanOrEqualTo<T>(expected);
	}
}