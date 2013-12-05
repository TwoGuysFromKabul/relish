package net.sf.relish;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * The available formats for data. For example, in HTTP requests and responses.
 */
public enum DataFormat {

	JSON, XML, TEXT, BINARY;

	private static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8',
			(byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f' };

	public static final Charset UTF8 = Charset.forName("UTF8");
	public static final Charset ASCII = Charset.forName("ASCII");

	private static final ObjectMapper MAPPER;

	static {
		MAPPER = new ObjectMapper();
		MAPPER.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
		MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
	}

	/**
	 * Converts the text used in the DSL to the bytes using this format.
	 * 
	 * @param text
	 *            The DSL representation of the data
	 * @return The bytes for the binary value. If text is null or empty then null is returned.
	 */
	public byte[] textToBytes(String text) {

		if (text == null || text.isEmpty()) {
			return null;
		}
		switch (this) {
		case JSON:
			try {
				MAPPER.readValue(text, JsonNode.class);
			} catch (IOException ex) {
				throw new RelishException(ex, "Invalid JSON: %s", text);
			}

			return text.getBytes(UTF8);
		case XML:
			// FIXME [jim] - do a better job with xml
		case TEXT:
			return text.getBytes(UTF8);
		case BINARY:
			return hexToBytes(text.replaceAll("\\s+", ""));
		default:
			throw new RelishException("Unknown enum value: %s. THIS IS A BUG!!", this);
		}
	}

	/**
	 * Converts the binary bytes to the text used in the DSL for this format.
	 * 
	 * @param bytes
	 *            The bytes for the binary value
	 * @return The {@link #normalizeText(String) normalized} text representation of the data. If bytes is null or empty then null is returned.
	 */
	public String bytesToText(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		switch (this) {
		case JSON:
		case XML:
		case TEXT:
			return normalizeText(new String(bytes, UTF8));
		case BINARY:
			return bytesToHex(bytes);
		default:
			throw new RelishException("Unknown enum value: %s. THIS IS A BUG!!", this);
		}
	}

	/**
	 * @return Normalized version of the specified text in this format. The text must be valid for this format. For example, if this format is {@link #JSON}
	 *         then the text must be valid JSON. The normalized version is what is used for comparisons in the DSL. The following changes are made to normalize
	 *         the specified text:
	 *         <ul>
	 *         <li>{@link #JSON}: Extraneous whitespace is removed.</li>
	 *         <li>{@link #XML}: The provided text is returned unchanged. This will be changed when XML support is improved.</li>
	 *         <li>{@link #TEXT}: The provided text is returned unchanged.</li>
	 *         <li>{@link #BINARY}" All whitespace is removed.</li>
	 *         </ul>
	 *         If text is null then null is returned.
	 */
	public String normalizeText(String text) {

		if (text == null) {
			return null;
		}

		switch (this) {
		case JSON:
			try {
				return compressAndFormatJson(text);
			} catch (IllegalArgumentException ex) {
				throw new RelishException("Unable to normalize the specified JSON payload. Input: %s", text);
			}
		case XML:
			// FIXME [jim] - do a better job with xml
		case TEXT:
			return text;
		case BINARY:
			return text.replaceAll("\\s+", "");
		default:
			throw new RelishException("Unknown enum value: %s. THIS IS A BUG!!", this);
		}
	}

	/**
	 * @return Normalized version of the specified regex for matching The normalized version is what is used for comparisons in the DSL. The following changes
	 *         are made to normalize the specified regex:
	 *         <ul>
	 *         <li>{@link #JSON}: Leading and trailing whitespace is removed from each line.</li>
	 *         <li>{@link #XML}: Leading and trailing whitespace is removed from each line.</li>
	 *         <li>{@link #TEXT}: The provided text is returned unchanged.</li>
	 *         <li>{@link #BINARY}" All whitespace is removed.</li>
	 *         </ul>
	 *         If regex is null then null is returned.
	 */
	public String normalizeRegex(String regex) {
		if (regex == null || regex.isEmpty()) {
			return regex;
		}

		switch (this) {
		case JSON:
		case XML:
			String[] parts = RelishUtil.quickSplit(regex, '\n');
			StringBuilder result = new StringBuilder();
			for (String part : parts) {
				result.append(part.trim());
			}
			return result.toString();
		case TEXT:
			return regex;
		case BINARY:
			return regex.replaceAll("\\s+", "");
		default:
			throw new RelishException("Unknown enum value: %s. THIS IS A BUG!!", this);
		}
	}

	private String bytesToHex(byte[] bytes) {

		if (bytes == null || bytes.length == 0) {
			return "";
		}

		byte[] hex = new byte[bytes.length * 2];

		int j = 0;
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i] & 0xff;

			hex[j++] = HEX_CHAR_TABLE[b >>> 4];
			hex[j++] = HEX_CHAR_TABLE[b & 0xf];
		}
		try {
			return new String(hex, "ASCII");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	private byte[] hexToBytes(String hex) {

		int len = hex.length();
		byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((charToHexDigit(hex, i) << 4) + charToHexDigit(hex, i + 1));
		}

		return data;
	}

	private int charToHexDigit(String hex, int index) {

		int digit = Character.digit(hex.charAt(index), 16);
		if (digit < 0) {
			throw new IllegalArgumentException("This is not a valid hexadecimal string: " + hex);
		}

		return digit;
	}

	private String compressAndFormatJson(String json) {
		try {
			JsonFactory factory = MAPPER.getFactory();
			JsonParser parser = factory.createParser(json);
			StringWriter writer = new StringWriter();
			JsonGenerator generator = factory.createGenerator(writer);
			while (parser.nextToken() != null) {
				generator.copyCurrentEvent(parser);
			}
			generator.close();

			JsonNode node = MAPPER.readValue(writer.getBuffer().toString(), JsonNode.class);
			Object o = MAPPER.treeToValue(node, Object.class);

			return MAPPER.writeValueAsString(o);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}
}
