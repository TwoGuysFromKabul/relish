package net.sf.relish.transformer;

import cucumber.api.Transformer;

/**
 * If the string to transform is null it is transformed into {@link Boolean#TRUE}. If the string is not null is is transformed into {@link Boolean#FALSE};
 */
public final class IsNullTransformer extends Transformer<Boolean> {

	/**
	 * @see cucumber.api.Transformer#transform(java.lang.String)
	 */
	@Override
	public Boolean transform(String value) {
		return value == null;
	}
}
