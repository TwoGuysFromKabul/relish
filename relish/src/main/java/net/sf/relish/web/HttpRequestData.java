package net.sf.relish.web;

/**
 * Contains the info for an HTTP Request. This class is thread safe.
 */
public final class HttpRequestData extends AbstractHttpRequestResponseData {

	private HttpMethod requestMethod;
	private String url;

	/**
	 * @return The request's HTTP method (GET, POST, etc)
	 */
	public synchronized HttpMethod getRequestMethod() {
		return requestMethod;
	}

	/**
	 * @param requestMethod
	 *            The request's HTTP method (GET, POST, etc)
	 */
	public synchronized void setRequestMethod(HttpMethod requestMethod) {
		this.requestMethod = requestMethod;
	}

	/**
	 * @param url
	 *            The URL for the request
	 */
	public synchronized void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return The URL for the request
	 */
	public synchronized String getUrl() {
		return url;
	}
}
