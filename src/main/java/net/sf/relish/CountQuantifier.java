package net.sf.relish;

import static net.sf.relish.matcher.RelishMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import org.hamcrest.Matcher;

/**
 * Evaluates count values (at most 5 messages, at least 2 connections, etc)
 */
public enum CountQuantifier {

	/**
	 * Matches an examined value that is at least (>=) a specified value
	 */
	AT_LEAST,

	/**
	 * Matches an examined value that is at most (<=) a specified value
	 */
	AT_MOST,

	/**
	 * Matches an examined value that is exactly (==) a specified value
	 */
	EXACTLY;

	/**
	 * @return A new {@link Matcher}, the type of which depends on the enum, for the specified value.
	 */
	public Matcher<Integer> newMatcher(int expected) {

		switch (this) {
		case AT_LEAST:
			return gte(expected);
		case AT_MOST:
			return lte(expected);
		case EXACTLY:
			return equalTo(expected);
		default:
			throw new RelishException("Unknown enum value: %s. THIS IS A BUG!!", this);
		}
	}
}
