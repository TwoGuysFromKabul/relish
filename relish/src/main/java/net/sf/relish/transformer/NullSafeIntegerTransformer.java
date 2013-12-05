package net.sf.relish.transformer;

import cucumber.api.Transformer;

/**
 * If the string to transform is not null it is transformed into the appropriate {@link Integer}. If the string is null is is transformed into 0;
 */
public final class NullSafeIntegerTransformer extends Transformer<Integer> {

	/**
	 * @see cucumber.api.Transformer#transform(java.lang.String)
	 */
	@Override
	public Integer transform(String value) {
		return value != null ? Integer.valueOf(value) : Integer.valueOf(0);
	}
}
