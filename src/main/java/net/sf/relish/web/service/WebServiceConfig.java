package net.sf.relish.web.service;

import static net.sf.relish.RelishUtil.*;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;

import net.sf.relish.ExpandingArrayList;
import net.sf.relish.RelishException;
import net.sf.relish.web.HttpMethod;
import net.sf.relish.web.HttpRequestData;
import net.sf.relish.web.HttpResponseData;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Configuration for mock web service processing by {@link WebServiceHandler}. This class is thread safe.
 */
final class WebServiceConfig {

	private final List<HttpRequestData> requests = new ExpandingArrayList<HttpRequestData>();
	private final List<HttpResponseData> responses = new ExpandingArrayList<HttpResponseData>();

	private boolean serverEnabled;
	private String target = "";

	/**
	 * Enables the handler for this config.
	 * 
	 * @return true if this config was enabled, false if it was already enabled
	 */
	public synchronized boolean enable(String target) {

		if (this.serverEnabled) {
			return false;
		}

		this.target = validateNotEmpty("target", target);
		this.serverEnabled = true;
		return true;
	}

	/**
	 * Disables the handler for this config.
	 * 
	 * @return true if this config was disabled, false if it was already disabled
	 */
	public synchronized boolean disable() {

		if (!this.serverEnabled) {
			return false;
		}

		this.serverEnabled = false;
		return true;
	}

	/**
	 * Called when a request is received.
	 * 
	 * @param request
	 *            The received request
	 * @return The data for the response. If this config is not {@link #enable(String) enable} the returned {@link HttpResponseData#getStatusCode()} will be
	 *         {@link HttpStatus#NOT_FOUND_404}. If no response has been configured for this request the returned {@link HttpResponseData#getStatusCode()} will
	 *         be {@link HttpStatus#SERVICE_UNAVAILABLE_503}.
	 */
	public synchronized HttpResponseData handleRequest(HttpServletRequest request) {

		if (!serverEnabled) {
			return new HttpResponseData(HttpStatus.NOT_FOUND_404);
		}

		HttpRequestData requestData = new HttpRequestData();

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			String value = request.getHeader(name);
			requestData.setHeader(name, value);
		}

		requestData.setRequestMethod(HttpMethod.valueOf(request.getMethod().toUpperCase()));
		String url = request.getQueryString() == null ? request.getRequestURL().toString() : request.getRequestURL() + request.getQueryString();
		requestData.setUrl(url);
		byte[] body = getRequestBody(request);
		requestData.setBody(body);

		requests.add(requestData);

		HttpResponseData responseData = responses.get(requests.size() - 1);
		return responseData != null ? responseData : new HttpResponseData(HttpStatus.SERVICE_UNAVAILABLE_503);
	}

	public synchronized String getTarget() {
		return target;
	}

	/**
	 * @return The {@link HttpRequestData request data} for the specified index. The first index is 1, not 0. If there is no request data for the specified
	 *         index a {@link RelishException} is thrown.
	 */
	public synchronized HttpRequestData getRequestData(int index) {

		validateGreaterThan("index", index, 0);

		HttpRequestData requestData = requests.get(index - 1);
		if (requestData == null) {
			throw new RelishException("Web service request %d does not exist", index);
		}

		return requestData;
	}

	/**
	 * @return The number of requests received by this web service.
	 */
	public synchronized int getRequestCount() {
		return requests.size();
	}

	/**
	 * @return The {@link HttpResponseData response data} for the specified index. The first index is 1, not 0. If there is no response data for the specified
	 *         index one is created and set on that index.
	 */
	public synchronized HttpResponseData getResponseData(int index) {

		validateGreaterThan("index", index, 0);

		index--;
		HttpResponseData responseData = responses.get(index);
		if (responseData == null) {
			responseData = new HttpResponseData();
			responses.set(index, responseData);
		}

		return responseData;
	}

	private byte[] getRequestBody(HttpServletRequest request) {

		try {
			String contentEncoding = request.getHeader("Content-encoding");
			InputStream in = request.getInputStream();
			if (in == null) {
				return null;
			}

			if ("gzip".equals(contentEncoding)) {
				in = new GZIPInputStream(in);
			}

			byte[] requestBody = IOUtils.toByteArray(in);
			return requestBody.length > 0 ? requestBody : null;

		} catch (Exception e) {
			throw new RelishException(e, "Failed to read HTTP request body");
		}
	}
}
