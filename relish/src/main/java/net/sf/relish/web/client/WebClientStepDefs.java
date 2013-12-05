package net.sf.relish.web.client;

import static net.sf.relish.RelishUtil.*;
import static net.sf.relish.matcher.RelishMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import net.sf.relish.RelishUtil;
import net.sf.relish.CountQuantifier;
import net.sf.relish.DataFormat;
import net.sf.relish.ExpandingArrayList;
import net.sf.relish.NameValuePair;
import net.sf.relish.RelishException;
import net.sf.relish.TableMatcher;
import net.sf.relish.transformer.CountQuantifierTransformer;
import net.sf.relish.web.HttpMethod;
import net.sf.relish.web.HttpRequestData;
import net.sf.relish.web.HttpResponseData;

import cucumber.api.Transform;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public final class WebClientStepDefs {

	private final List<HttpRequestData> requests = new ExpandingArrayList<HttpRequestData>();
	private final List<HttpResponseData> responses = new ExpandingArrayList<HttpResponseData>();

	/**
	 * Configures the specified web client request body
	 * 
	 * @param startIndex
	 *            The index of the first request in the range to configure: 1 is the first request, 2 is the seconds request, etc
	 * @param endIndex
	 *            The index of the last request in the range to configure: 1 is the first request, 2 is the seconds request, etc. If null then startIndex is
	 *            used.
	 * @param format
	 *            The format of the request body
	 * @param body
	 *            The request body
	 */
	@Given("^web client requests? (\\d+)(?: thru (\\d+))? body is this (JSON|XML|text|binary):$")
	public void webClientRequestBodyIs(int startIndex, Integer endIndex, DataFormat format, String body) {

		byte[] bodyBytes = format.textToBytes(body);
		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {
			getOrCreateRequestData(i).setBody(bodyBytes);
		}
	}

	/**
	 * Configures a request header for the specified web client.
	 * 
	 * @param startIndex
	 *            The index of the first request in the range to configure: 1 is the first request, 2 is the seconds request, etc
	 * @param endIndex
	 *            The index of the last request in the range to configure: 1 is the first request, 2 is the seconds request, etc. If null then startIndex is
	 *            used.
	 * @param headerName
	 *            The name of the header
	 * @param headerValue
	 *            The value of the header
	 */
	@Given("^web client requests? (\\d+)(?: thru (\\d+))? header \"(\\S+)\" is \"(\\S+)\"$")
	public void webClientRequestHeaderIs(int startIndex, Integer endIndex, String headerName, String headerValue) {

		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {
			getOrCreateRequestData(i).setHeader(headerName, headerValue);
		}
	}

	/**
	 * Configures multiple request headers for the specified web client. The values are in a table with "Name" and "Value" columns like this:<code><pre>
	 * Given web client request 1 uses these headers:
	 * | Name | Value |
	 * | header1 | value1 |
	 * | header2 | value2 |
	 * ...
	 * </pre></code>
	 * 
	 * @param startIndex
	 *            The index of the first request in the range to configure: 1 is the first request, 2 is the seconds request, etc
	 * @param endIndex
	 *            The index of the last request in the range to configure: 1 is the first request, 2 is the seconds request, etc. If null then startIndex is
	 *            used.
	 * @param headers
	 *            List of the headers to include in the request
	 */
	@Given("^web client requests? (\\d+)(?: thru (\\d+))? uses? these headers:$")
	public void webClientRequestUsesHeaders(int startIndex, Integer endIndex, List<NameValuePair> headers) {

		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {
			for (NameValuePair header : headers) {
				getOrCreateRequestData(i).setHeader(header.getName(), header.getValue());
			}
		}
	}

	/**
	 * Sends a web client request to a specified URL
	 * 
	 * @param startIndex
	 *            The index of the first request in the range to configure: 1 is the first request, 2 is the seconds request, etc
	 * @param endIndex
	 *            The index of the last request in the range to configure: 1 is the first request, 2 is the seconds request, etc. If null then startIndex is
	 *            used.
	 * @param url
	 *            The URL to send the request to
	 * @param method
	 *            The HTTP method used to send the request
	 */
	@When("^web client requests? (\\d+)(?: thru (\\d+))?(?: is| are)? sent to \"(http://.*)\" using method \"(GET|POST|HEAD|PUT|OPTIONS|DELETE|TRACE|CONNECT|MOVE)\"$")
	public void webClientRequestIsSentTo(int startIndex, Integer endIndex, String url, HttpMethod method) throws Exception {

		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {

			HttpRequestData requestData = getOrCreateRequestData(i);
			requestData.setUrl(url);
			requestData.setRequestMethod(method);
			byte[] body = requestData.getBody();
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoOutput(body != null);
			conn.setRequestMethod(requestData.getRequestMethod().toString());
			for (NameValuePair header : requestData.getHeaders()) {
				conn.setRequestProperty(header.getName(), header.getValue());
			}
			conn.connect();

			if (body != null) {
				RelishUtil.writeToOutputStream(conn.getOutputStream(), requestData.getBody());
			}

			HttpResponseData responseData = getOrCreateResponseData(responses.size() + 1);
			int statusCode = conn.getResponseCode();
			responseData.setStatusCode(statusCode);
			for (String name : conn.getHeaderFields().keySet()) {
				if (name != null) {
					responseData.setHeader(name, conn.getHeaderField(name));
				}
			}
			responseData.setBody(getResponseBody(conn));

			conn.disconnect();
		}
	}

	private byte[] getResponseBody(HttpURLConnection conn) {

		byte[] body = null;
		InputStream in;
		try {
			in = conn.getInputStream();
		} catch (IOException e) {
			in = conn.getErrorStream();
		}
		if (in != null) {
			body = RelishUtil.readFromInputStream(in);
		}

		return body;
	}

	/**
	 * Validates a web client response body
	 * 
	 * @param startIndex
	 *            The index of the first request in the range to configure: 1 is the first request, 2 is the seconds request, etc
	 * @param endIndex
	 *            The index of the last request in the range to configure: 1 is the first request, 2 is the seconds request, etc. If null then startIndex is
	 *            used.
	 * @param bodyFormat
	 *            The format the body is in. For binary this should be space delimited hexadecimal.
	 * @param bodyRegex
	 *            A regular expression which the response body must match. If the format is "binary" then the body is hexadecimal text. If the body contains
	 *            multiple lines each line will have all leading and trailing whitespace removed then all lines will be concatenated into a single line.
	 */
	@Then("^web client responses? (\\d+)(?: thru (\\d+))? body should match this (JSON|XML|text|binary):$")
	public void webClientResponseBodyShouldBe(int startIndex, Integer endIndex, DataFormat bodyFormat, String bodyRegex) {

		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {
			String bodyText = bodyFormat.bytesToText(getRequiredResponseData(i).getBody());
			bodyRegex = bodyFormat.normalizeRegex(bodyRegex);
			assertThat(bodyText, matches(bodyRegex), "Web client response %d body does not match", i);
		}
	}

	/**
	 * Validates the headers received in a response to the specified web client. The values are in a table with "Name" and "Value" columns like this:
	 * <code><pre>
	 * Then web client response 1 headers should include:
	 * | Name | Value |
	 * | header1 | value1 |
	 * | header2 | value2 |
	 * ...
	 * </pre></code>
	 * 
	 * @param startIndex
	 *            The index of the first request in the range to configure: 1 is the first request, 2 is the seconds request, etc
	 * @param endIndex
	 *            The index of the last request in the range to configure: 1 is the first request, 2 is the seconds request, etc. If null then startIndex is
	 *            used.
	 * @param headersMatcher
	 *            The method used to compare the specified headers with the actual response headers:
	 *            <ul>
	 *            <li>be: All the headers in the response must be equal to all the specified headers. Any headers in the response not specified or vice-versa
	 *            will fail the comparison.</li>
	 *            <li>include: All the headers specified must equal headers in the response but any headers in the response not specified are ignored.</li>
	 *            </ul>
	 * @param headers
	 *            The headers to compare
	 */
	@Then("^web client responses? (\\d+)(?: thru (\\d+))? headers should (be|include):$")
	public void webClientResponseHeadersShouldBe(int startIndex, Integer endIndex, TableMatcher headersMatcher, List<NameValuePair> headers) {

		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {
			assertThat(getRequiredResponseData(i).getHeaders(), headersMatcher.newMatcher(headers), "Web client response %d headers do not match", i);
		}
	}

	/**
	 * Validates a single header received in a response to the specified web client.
	 * 
	 * @param startIndex
	 *            The index of the first request in the range to configure: 1 is the first request, 2 is the seconds request, etc
	 * @param endIndex
	 *            The index of the last request in the range to configure: 1 is the first request, 2 is the seconds request, etc. If null then startIndex is
	 *            used.
	 * @param headerMatcher
	 *            The method used to compare the specified header value with the actual response header value:
	 *            <ul>
	 *            <li>be: The values are compared for equality</li>
	 *            <li>matches: The response header value must match the specified regular expression</li>
	 *            </ul>
	 * @param headerName
	 *            The name of the header to compare
	 * @param headerValueRegex
	 *            A regular expression which the header value must match.
	 */
	@Then("^web client responses? (\\d+)(?: thru (\\d+))? header \"(\\S+)\" should match \"(\\S+)\"$")
	public void webClientResponseHeaderShouldBe(int startIndex, Integer endIndex, String headerName, String headerValueRegex) {

		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {
			assertThat(getRequiredResponseData(i).getHeaderValue(headerName), matches(headerValueRegex), "Web client response %d header %s does not match", i,
					headerName);
		}
	}

	/**
	 * Validates the HTTP status code received in a response to the specified web client
	 * 
	 * @param startIndex
	 *            The index of the first request in the range to configure: 1 is the first request, 2 is the seconds request, etc
	 * @param endIndex
	 *            The index of the last request in the range to configure: 1 is the first request, 2 is the seconds request, etc. If null then startIndex is
	 *            used.
	 * @param statusCode
	 *            The status code the response should contain
	 */
	@Then("^web client responses? (\\d+)(?: thru (\\d+))? status code should be (\\d{3})$")
	public void webClientResponseStatusCodeShouldBe(int startIndex, Integer endIndex, int statusCode) {

		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {
			assertThat(getRequiredResponseData(i).getStatusCode(), equalTo(statusCode), "Web client response %d status codes do not match", i);
		}
	}

	/**
	 * Validates the number of responses received by the specified web client.
	 * 
	 * @param responseCountQuantifier
	 *            How to evaluate the response count
	 * @param responseCount
	 *            The number of responses to match
	 */
	@Then("^web client should have (at least|at most|exactly) (\\d+) responses?$")
	public void webClientShouldHaveResponseCount(@Transform(CountQuantifierTransformer.class) CountQuantifier responseCountQuantifier, int responseCount) {

		assertThat(responses.size(), responseCountQuantifier.newMatcher(responseCount), "Web client response counts do not match");
	}

	private HttpRequestData getOrCreateRequestData(int index) {

		index--;
		HttpRequestData requestData = requests.get(index);
		if (requestData == null) {
			requestData = new HttpRequestData();
			requests.set(index, requestData);
		}

		return requestData;
	}

	private HttpResponseData getRequiredResponseData(int index) {

		HttpResponseData responseData = responses.get(index - 1);
		if (responseData == null) {
			throw new RelishException("Web client response %d does not exist", index);
		}
		return responseData;
	}

	private HttpResponseData getOrCreateResponseData(int index) {

		index--;
		HttpResponseData responseData = responses.get(index);
		if (responseData == null) {
			responseData = new HttpResponseData();
			responses.set(index, responseData);
		}

		return responseData;
	}

	private int getEndIndex(int startIndex, Integer endIndex) {
		return endIndex == null ? startIndex : endIndex;
	}
}
