package net.sf.relish;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

/**
 * Utility methods for use in the relish project
 */
public final class RelishUtil {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	/**
	 * This is a convenience method for the JUnit {@link Assert#assertThat(String, Object, Matcher)} method. This allows printf (String.format) style reason
	 * text. The following is copied from {@link Assert#assertThat(String, Object, Matcher)}:
	 * <p>
	 * Asserts that <code>actual</code> satisfies the condition specified by <code>matcher</code>. If not, an {@link AssertionError} is thrown with the reason
	 * and information about the matcher and failing value. Example:
	 * 
	 * <pre>
	 *   assertThat(&quot;Help! Integers don't work&quot;, 0, is(1)); // fails:
	 *     // failure message:
	 *     // Help! Integers don't work
	 *     // expected: is &lt;1&gt;
	 *     // got value: &lt;0&gt;
	 *   assertThat(&quot;Zero is one&quot;, 0, is(not(1))) // passes
	 * </pre>
	 * 
	 * <code>org.hamcrest.Matcher</code> does not currently document the meaning of its type parameter <code>T</code>. This method assumes that a matcher typed
	 * as <code>Matcher&lt;T&gt;</code> can be meaningfully applied only to values that could be assigned to a variable of type <code>T</code>.
	 * 
	 * @param <T>
	 *            the static type accepted by the matcher (this can flag obvious compile-time problems such as {@code assertThat(1, is("a"))}
	 * @param actual
	 *            the computed value being compared
	 * @param matcher
	 *            an expression, built of {@link Matcher}s, specifying allowed values
	 * @param reasonFormat
	 *            printf style format for additional information about the error
	 * @param reasonArgs
	 *            Args for reasonFormat
	 * 
	 * @see org.hamcrest.CoreMatchers
	 * @see org.hamcrest.MatcherAssert </p>
	 */
	public static <T> void assertThat(T actual, Matcher<? super T> matcher, String reasonFormat, Object... reasonArgs) {
		MatcherAssert.assertThat(String.format(reasonFormat, reasonArgs), actual, matcher);
	}

	/**
	 * Polls {@link #assertThat(Object, Matcher, String, Object...)} with the value returned by the {@link Callable actualAccessor} until either it does not
	 * throw {@link AssertionError} or the specified timout occurs. If the timeout occurs the last AssertionError caught is rethrown. If timeout == 0 then
	 * timeUnit is ignored and may be null.
	 */
	public static <T> void assertThatWithin(long timeout, TimeUnit timeUnit, Callable<T> actualAccessor, Matcher<? super T> matcher, String reasonFormat,
			Object... reasonArgs) {

		long timeoutMillis = timeout == 0 ? 0 : TimeUnit.MILLISECONDS.convert(timeout, timeUnit);

		int soFar = 0;
		AssertionError error = null;
		while (soFar <= timeoutMillis) {
			try {
				try {
					assertThat(actualAccessor.call(), matcher, reasonFormat, reasonArgs);
					return;
				} catch (Exception e) {
					throw new RuntimeException("Failed to get actual value for comparison from callable", e);
				}
			} catch (AssertionError e) {
				error = e;
				soFar += 10;
				if (soFar <= timeoutMillis) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						throw new RuntimeException("Thread interrupted while waiting for assertion to succeed", e1);
					}
				}
			}
		}
		if (error != null) {
			throw error;
		}
	}

	/**
	 * Delegates to {@link #assertThatWithin(long, TimeUnit, Callable, Matcher, String, Object...)} with no reason specified
	 */
	public static <T> void assertThatWithin(long timout, TimeUnit timeUnit, Callable<T> actualAccessor, Matcher<? super T> matcher) {
		assertThatWithin(timout, timeUnit, actualAccessor, matcher, "");
	}

	/**
	 * Splits value using the specified delimiter. This should be faster than most other split methods like {@link String#split(String)} and
	 * {@link StringUtils#split(String, char)}. Examples:
	 * <ul>
	 * <li>quickSplit(null, delimiter) = {}</li>
	 * <li>quickSplit("", delimiter) = {}</li>
	 * <li>quickSplit("-", delimiter) = {}</li>
	 * <li>quickSplit("--", delimiter) = {}</li>
	 * <li>quickSplit("---", delimiter) = {}</li>
	 * <li>quickSplit("abc", delimiter) = {"abc"}</li>
	 * <li>quickSplit("-abc", delimiter) = {"abc"}</li>
	 * <li>quickSplit("abc-", delimiter) = {"abc"}</li>
	 * <li>quickSplit("-abc-", delimiter) = {"abc"}</li>
	 * <li>quickSplit("abc-123", delimiter) = {"abc","123"}</li>
	 * <li>quickSplit("abc--123", delimiter) = {"abc","123"}</li>
	 * <li>quickSplit("abc---123", delimiter) = {"abc","123"}</li>
	 * <li>quickSplit("--abc---123--", delimiter) = {"abc","123"}</li>
	 * </ul>
	 * 
	 * @param value
	 *            String to split
	 * @param delimiter
	 *            Splits the string at this delimiter. The delimiter is not included in the output. Any beginning or ending delimiter is ignored. Sequential
	 *            delimiters will be treated as a single delimiter.
	 * 
	 * @return The parts of the string after the split
	 */
	public static String[] quickSplit(String value, char delimiter) {

		if (value == null || value.isEmpty()) {
			return EMPTY_STRING_ARRAY;
		}

		int count = getSplitStringCount(value, delimiter);

		if (count == 0) {
			return EMPTY_STRING_ARRAY;
		}

		String[] result = new String[count];

		int start = 0;
		int index = 0;
		char lastChar = delimiter;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c != delimiter && lastChar == delimiter) {
				start = i;
			} else if (c == delimiter && lastChar != delimiter) {
				result[index++] = value.substring(start, i);
			}
			lastChar = c;
		}

		if (index == count - 1) {
			result[index++] = value.substring(start, value.length());
		}

		return result;
	}

	/**
	 * Closes the {@link Closeable closeable} ignoring any exceptions as well as a <code>null</code> object to close.
	 * 
	 * @param closeable
	 *            The {@link Closeable closeable} to close
	 */
	public static void closeQuietly(Closeable closeable) {

		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {
			// ignore
		}
	}

	/**
	 * Read all of the data that is available from an {@link InputStream input stream}. No decoration is applied by this method; the data from the input stream
	 * is read as a {@code byte} stream.
	 * 
	 * The input stream passed in as a parameter to this method is not closed by this method.
	 * 
	 * @param in
	 *            The {@link InputStream input stream} to read the data from
	 * 
	 * @return A byte array that contains all of the data read from the specified input stream
	 */
	public static byte[] readFromInputStream(InputStream in) {
		validateNotNull("input stream", in);

		try {
			byte[] buffer = new byte[8192];
			int bytesRead = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			while ((bytesRead = in.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}

			return baos.toByteArray();
		} catch (Exception ex) {
			throw new RuntimeException("Unable to read from the specified input stream.", ex);
		}
	}

	/**
	 * Writes all of the data in the bytes[] to the {@link OutputStream output stream}.
	 * 
	 * The output stream passed in as a parameter to this method is not closed by this method.
	 * 
	 * @param out
	 *            The {@link OutputStream output stream} to write the data to
	 */
	public static void writeToOutputStream(OutputStream out, byte[] bytes) {
		validateNotNull("output stream", out);

		if (bytes == null) {
			return;
		}

		try {
			out.write(bytes);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to write to the specified output stream.", ex);
		}
	}

	/**
	 * Get the entire contents of a single file as a {@link String string}. The conversion into a string of the file contents is done using the US ASCII
	 * {@link Charset charset}.
	 * 
	 * @param file
	 *            The {@link File file} to read data from
	 * 
	 * @return A {@link String string} that contains the contents of the entire file, converted to US ASCII format
	 * 
	 * @throws IllegalArgumentException
	 *             If <code>file</code> is <code>null</code>
	 * @throws RuntimeException
	 *             If <code>file</code> is not a file, if <code>file</code> cannot be read (does not have the read permission), or if an exception occurs during
	 *             reading of the file
	 */
	public static String getFileContentsAsString(File file) {

		return new String(getFileContents(file), Charset.forName("US-ASCII"));
	}

	/**
	 * Get the entire contents of a single file as a byte stream.
	 * 
	 * @param file
	 *            The {@link File file} to read data from
	 * 
	 * @return A <code>byte</code> array that comprises a byte stream that contains the contents of the entire file
	 * 
	 * @throws IllegalArgumentException
	 *             If <code>file</code> is <code>null</code>
	 * @throws RuntimeException
	 *             If <code>file</code> is not a file, if <code>file</code> cannot be read (does not have the read permission), or if an exception occurs during
	 *             reading of the file
	 */
	public static byte[] getFileContents(File file) {

		assertFile(file);

		FileInputStream stream = null;
		FileChannel channel = null;
		try {
			stream = new FileInputStream(file);
			channel = stream.getChannel();
			final ByteBuffer buffer = ByteBuffer.allocate(8192);
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			final byte[] holdingTank = new byte[8192];

			int bytesRead = -1;
			while ((bytesRead = channel.read(buffer)) != -1) {
				buffer.flip();
				buffer.get(holdingTank, 0, bytesRead);
				buffer.flip();

				outputStream.write(holdingTank, 0, bytesRead);
			}
			channel.close();
			stream.close();

			return outputStream.toByteArray();
		} catch (final Exception ex) {
			throw new RuntimeException(String.format("Unable to read the contents of the file %s due to an exception " + "being thrown.", file.getName()), ex);
		} finally {
			closeQuietly(channel);
			closeQuietly(stream);
		}
	}

	/**
	 * Writes the contents of the {@link InputStream} to the file completely overwriting existing content. The {@link InputStream} is not closed after reading.
	 * It's up to the caller to properly close.
	 * 
	 * @param file
	 *            file to write to.
	 * @param inputStream
	 *            data to write. The inputStream is not closed. It's up to the caller to properly close.
	 * @throws IOException
	 */
	public static void writeToFile(File file, InputStream inputStream) throws IOException {

		BufferedInputStream bis = new BufferedInputStream(inputStream);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			byte[] bytes = new byte[256];
			int bytesRead;
			while ((bytesRead = bis.read(bytes)) != -1) {
				fos.write(bytes, 0, bytesRead);
			}
		} finally {
			closeQuietly(fos);
		}

	}

	/**
	 * Validates that value is not null or empty.
	 */
	public static String validateNotEmpty(String name, String value) {

		if (value == null || value.trim().isEmpty()) {
			doThrow("Argument %s must not be null or empty", name);
		}

		return value;
	}

	/**
	 * Validates that value is not null
	 */
	public static <T> T validateNotNull(String name, T value) {

		if (value == null) {
			doThrow("Argument %s must not be null", name);
		}

		return value;
	}

	/**
	 * Validates that value is greater than min. This converts all values to longs so it should only be used with integral types including Atomic... integral
	 * types.
	 */
	public static <T extends Number> T validateGreaterThan(String name, T value, T min) {

		if (value.longValue() <= min.longValue()) {
			doThrow("Argument %s must be greater than %d: was %d", name, min, value);
		}

		return value;
	}

	/**
	 * Validates the value is between min and max, inclusive. This converts all values to longs so it should only be used with integral types including
	 * Atomic... integral types.
	 */
	public static <T extends Number> T validateInRange(String name, T value, T min, T max) {

		if (value.longValue() < min.longValue() || value.longValue() > max.longValue()) {
			doThrow("Argument %s must be between %d and %d, inclusive: was %d", name, min, max, value);
		}

		return value;
	}

	private static void doThrow(String format, Object... args) {

		throw new IllegalArgumentException(String.format(format, args));
	}

	private static void assertFile(File file) {

		if (file == null) {
			throw new IllegalArgumentException("The file cannot be null.");
		}
		if (!file.isFile() || !file.canRead()) {
			throw new RuntimeException(String.format("The file %s is either not a file or cannot be read.", file.getName()));
		}
	}

	private static int getSplitStringCount(String value, char delimiter) {

		int count = 0;
		char lastChar = delimiter;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c != delimiter && lastChar == delimiter) {
				count++;
			}
			lastChar = c;
		}

		return count;
	}

	private RelishUtil() {
	}
}
