package net.sf.relish.matcher;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * A {@link Matcher} that matches when the examined {@link Comparable} is less than the expected comparable: <code>examined.compareTo(expected) < 0</code>
 */
final class LessThan<T extends Comparable<T>> extends AbstractTypeSafeMatcher<T> {

	private LessThan(T expected) {
		super(expected);
	}

	/**
	 * @see org.hamcrest.TypeSafeMatcher#matchesSafely(java.lang.Object)
	 */
	@Override
	protected boolean matchesSafely(T item) {
		return item.compareTo(expected) < 0;
	}

	/**
	 * Creates a matcher that matches when the examined {@link Comparable} is less than the expected comparable: <code>examined.compareTo(expected) < 0</code>
	 */
	@Factory
	public static <T extends Comparable<T>> Matcher<T> lessThan(T expected) {
		return new LessThan<T>(expected);
	}
}