package net.sf.relish.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.relish.NameValuePair;

/**
 * Base class for {@link HttpRequestData} and {@link HttpResponseData}. This class is thread safe.
 */
abstract class AbstractHttpRequestResponseData {

	private final Map<String, NameValuePair> headersByName = new HashMap<String, NameValuePair>();
	private byte[] body;

	public AbstractHttpRequestResponseData() {
	}

	public AbstractHttpRequestResponseData(byte[] body) {
		this.body = copy(body);
	}

	/**
	 * Sets the specified header to the specified value's toString
	 */
	public final synchronized void setHeader(String name, Object value) {
		headersByName.put(name, new NameValuePair(name, value.toString()));
	}

	/**
	 * Sets the specified header
	 */
	public final synchronized void setHeader(NameValuePair header) {
		headersByName.put(header.getName(), header);
	}

	/**
	 * @return The header with the specified name
	 */
	public final synchronized NameValuePair getHeader(String name) {
		return headersByName.get(name);
	}

	/**
	 * @return The value of the header with the specified name
	 */
	public final synchronized String getHeaderValue(String name) {

		NameValuePair header = getHeader(name);
		return header == null ? null : header.getValue();
	}

	/**
	 * @return Collection of {@link NameValuePair}s
	 */
	public final synchronized List<NameValuePair> getHeaders() {
		return new ArrayList<NameValuePair>(headersByName.values());
	}

	/**
	 * @return The body of the response.
	 */
	public final synchronized byte[] getBody() {
		return copy(body);
	}

	/**
	 * @param body
	 *            The body of the response.
	 */
	public final synchronized void setBody(byte[] body) {
		this.body = copy(body);
	}

	private byte[] copy(byte[] src) {

		if (src == null) {
			return null;
		}
		byte[] dest = new byte[src.length];
		System.arraycopy(src, 0, dest, 0, src.length);
		return dest;
	}
}
