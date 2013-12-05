package net.sf.relish.matcher;

import java.util.regex.Pattern;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * A {@link Matcher} that matches when the examined String matches the specified regular expression
 */
final class Matches extends AbstractTypeSafeMatcher<String> {

	private Matches(String expectedRegex) {
		super(expectedRegex);
	}

	/**
	 * @see org.hamcrest.TypeSafeMatcher#matchesSafely(java.lang.Object)
	 */
	@Override
	protected boolean matchesSafely(String item) {

		return Pattern.matches(expected, item);
	}

	/**
	 * Creates a matcher that matches when the examined string matches the specified regular expression
	 */
	@Factory
	public static Matcher<String> matches(String regex) {
		return new Matches(regex);
	}
}