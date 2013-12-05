package net.sf.relish.transformer;

import cucumber.api.Transformer;

/**
 * Base class that can be extended to provide a Cucumber {@link Transformer transformer} that translates a string into {@link Boolean#TRUE},
 * {@link Boolean#FALSE}, or null. The value being compared will be trimmed of all leading and trailing whitespace.
 * 
 * @param <T>
 *            This will always be {@link Boolean} but it needs to be specified in extending classes for the generic type to be correctly detected.
 */
public abstract class StringToBooleanTransformer<T> extends Transformer<Boolean> {

	private final String falseValue;
	private final String trueValue;
	private final boolean nullIsFalse;

	/**
	 * The value compared to these will be trimmed before being compared.
	 * 
	 * @param trueValue
	 *            The value that will be transformed to {@link Boolean#TRUE}
	 * @param falseValue
	 *            The value that will be transformed to {@link Boolean#FALSE}
	 */
	protected StringToBooleanTransformer(String trueValue, String falseValue, boolean nullIsFalse) {
		this.trueValue = trueValue;
		this.falseValue = falseValue;
		this.nullIsFalse = nullIsFalse;
	}

	/**
	 * @see cucumber.api.Transformer#transform(java.lang.String)
	 */
	@Override
	public final Boolean transform(String value) {
		if (value == null) {
			return nullIsFalse ? false : null;
		}

		value = value.trim();

		if (trueValue.equals(value)) {
			return true;
		}
		if (falseValue.equals(value)) {
			return false;
		}

		throw new IllegalArgumentException(String.format("Invalid value: '%s'. Only '%s', '%s', or null are allowed", value, trueValue, falseValue));
	}
}
