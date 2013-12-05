package net.sf.relish.transformer;

import cucumber.api.Transformer;

/**
 * If the string to transform is not null it is transformed into {@link Boolean#TRUE}. If the string is null is is transformed into {@link Boolean#FALSE};
 */
public final class IsNotNullTransformer extends Transformer<Boolean> {

	/**
	 * @see cucumber.api.Transformer#transform(java.lang.String)
	 */
	@Override
	public Boolean transform(String value) {
		return value != null;
	}
}
