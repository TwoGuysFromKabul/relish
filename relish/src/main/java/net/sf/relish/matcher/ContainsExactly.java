package net.sf.relish.matcher;

import java.util.Collection;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * A {@link Matcher} that matches when a {@link Collection} contains all the elements of another {@link Collection} and they are the same size.
 */
final class ContainsExactly<T> extends AbstractTypeSafeMatcher<Collection<T>> {

	private ContainsExactly(Collection<T> expected) {
		super(expected);
	}

	/**
	 * @see org.hamcrest.TypeSafeMatcher#matchesSafely(java.lang.Object)
	 */
	@Override
	protected boolean matchesSafely(Collection<T> item) {

		return item.size() == expected.size() && item.containsAll(expected);
	}

	/**
	 * Creates a matcher that matches when the examined Collection contains all the elements from the specified Collection and both Collections are the same
	 * size.
	 */
	@Factory
	public static <T> Matcher<Collection<T>> containsExactly(Collection<T> expected) {
		return new ContainsExactly<T>(expected);
	}
}