package net.sf.relish.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Base for {@link TypeSafeMatcher}s that provides a decent description based on class name.
 */
abstract class AbstractTypeSafeMatcher<T> extends TypeSafeMatcher<T> {

	/**
	 * The exptect value. For use by extending classes
	 */
	final T expected;

	public AbstractTypeSafeMatcher(T expected) {
		this.expected = expected;
	}

	/**
	 * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
	 */
	@Override
	public void describeTo(Description description) {
		description.appendText(getClass().getSimpleName() + " ");
		description.appendValue(expected);
	}
}
