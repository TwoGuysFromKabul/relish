package net.sf.relish;

/**
 * Thrown when an exception occurs in relish
 */
public class RelishException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RelishException(String format, Object... args) {

		super(String.format(format, args));
	}

	public RelishException(Throwable cause, String format, Object... args) {

		super(String.format(format, args), cause);
	}

}
