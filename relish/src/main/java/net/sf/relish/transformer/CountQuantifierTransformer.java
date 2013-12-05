package net.sf.relish.transformer;

import net.sf.relish.CountQuantifier;

import cucumber.api.Transformer;

/**
 * Cucumber {@link Transformer} that converts the strings "at least", "at most", and "exactly" into a {@link CountQuantifier}.
 */
public final class CountQuantifierTransformer extends Transformer<CountQuantifier> {

	/**
	 * @see cucumber.api.Transformer#transform(java.lang.String)
	 */
	@Override
	public CountQuantifier transform(String value) {
		return CountQuantifier.valueOf(value.trim().replace(' ', '_').toUpperCase());
	}

}
