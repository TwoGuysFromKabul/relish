package net.sf.relish;

import static net.sf.relish.matcher.RelishMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Collection;

import org.hamcrest.Matcher;

/**
 * Each enumerated value is a different algorithm used to match tables in the DSL.
 */
public enum TableMatcher {

	/**
	 * All the rows in the actual table must exist in the expected table and vice-versa and they must be in the same order.
	 */
	EQUAL,

	/**
	 * All the rows in the actual table must exist in the expected table and vice-versa but their order does not have to match.
	 */
	BE,

	/**
	 * All the entries in the expected table must be in the actual table but there may be entries in the actual table that are not in the expected table.
	 */
	INCLUDE;

	public <T> Matcher<Collection<T>> newMatcher(Collection<T> expected) {

		if (expected == null) {
			return equalTo(expected);
		}

		switch (this) {
		case EQUAL:
			return equalTo(expected);
		case BE:
			return containsExactly(expected);
		case INCLUDE:
			return contains(expected);
		default:
			throw new RelishException("Unknown enum value %s. THIS IS A BUG!!", this);
		}
	}
}
