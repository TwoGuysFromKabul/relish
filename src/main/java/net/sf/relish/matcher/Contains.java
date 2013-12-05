package net.sf.relish.matcher;

import java.util.Collection;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * A {@link Matcher} that matches when a {@link Collection} contains all the elements of another {@link Collection}
 */
final class Contains<T> extends AbstractTypeSafeMatcher<Collection<T>> {

	private Contains(Collection<T> expected) {
		super(expected);
	}

	/**
	 * @see org.hamcrest.TypeSafeMatcher#matchesSafely(java.lang.Object)
	 */
	@Override
	protected boolean matchesSafely(Collection<T> item) {

		return item.containsAll(expected);
	}

	/**
	 * Creates a matcher that matches when the examined Collection contains all the elements from the expected Collection
	 */
	@Factory
	public static <T> Matcher<Collection<T>> contains(Collection<T> expected) {
		return new Contains<T>(expected);
	}
}