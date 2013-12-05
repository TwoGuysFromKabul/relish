package net.sf.relish.web;

/**
 * Contains the info for an HTTP response. This class is thread safe
 */
public final class HttpResponseData extends AbstractHttpRequestResponseData {

	private int statusCode = 200;
	private long delayMillis;

	public HttpResponseData() {
	}

	public HttpResponseData(int statusCode) {
		this(null, statusCode);
	}

	public HttpResponseData(byte[] body) {
		this(body, 200);
	}

	public HttpResponseData(byte[] body, int statusCode) {
		super(body);
		this.statusCode = statusCode;
	}

	/**
	 * @return The HTTP status code returned in the response
	 */
	public synchronized int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode
	 *            The HTTP status code returned in the response
	 */
	public synchronized void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return Millis to delay this response
	 */
	public synchronized long getDelayMillis() {
		return delayMillis;
	}

	/**
	 * @param delayMillis
	 *            Millis to delay this response
	 */
	public synchronized void setDelayMillis(long delayMillis) {
		this.delayMillis = delayMillis;
	}
}
