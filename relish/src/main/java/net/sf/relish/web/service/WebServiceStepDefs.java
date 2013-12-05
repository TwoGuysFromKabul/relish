package net.sf.relish.web.service;

import static net.sf.relish.RelishUtil.*;
import static net.sf.relish.matcher.RelishMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import net.sf.relish.CountQuantifier;
import net.sf.relish.DataFormat;
import net.sf.relish.NameValuePair;
import net.sf.relish.RelishException;
import net.sf.relish.TableMatcher;
import net.sf.relish.transformer.CountQuantifierTransformer;
import net.sf.relish.web.HttpMethod;
import net.sf.relish.web.HttpRequestData;
import net.sf.relish.web.HttpResponseData;

import cucumber.api.Transform;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public final class WebServiceStepDefs {

	private final Map<String, WebServiceConfig> webServiceConfigByName = Collections.synchronizedMap(new HashMap<String, WebServiceConfig>());
	private final WebServiceServer server = new WebServiceServer(new WebServiceHandler(webServiceConfigByName));

	/**
	 * Runs after each relish scenario
	 */
	@After
	public void after() throws Exception {
		server.stopServer();
	}

	/**
	 * Starts a mock web service with the specified name at the specified port and path
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param serverPort
	 *            The port to run the service on. All web services must be run on the same port.
	 * @param path
	 *            The path to the web service. This would be the "context root" if the service were a web app.
	 */
	@Given("^web service \"(\\S.+\\S)\" is running at \"http://localhost:(\\d{2,5})(/\\S*)\"$")
	public void webServiceIsRunningAt(String webServiceName, int serverPort, String path) throws Exception {

		WebServiceConfig config = getOrCreateWebServiceConfig(webServiceName);

		server.startServer(serverPort);

		if (!config.enable(path)) {
			throw new RelishException("You cannot start web service %s because it is already running", webServiceName);
		}
	}

	/**
	 * Stops the specified web service
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 */
	@Given("^web service \"(\\S.+\\S)\" is stopped$")
	public void webServiceIsStopped(String webServiceName) throws Exception {

		WebServiceConfig config = getRequiredWebServiceConfig(webServiceName);

		if (config == null || !config.disable()) {
			throw new RelishException("You cannot stop web service %s because it is not running", webServiceName);
		}
	}

	/**
	 * Configures the specified mock web service response body
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param startIndex
	 *            The index of the first response in the range to configure: 1 is the first response, 2 is the seconds response, etc
	 * @param endIndex
	 *            The index of the last response in the range to configure: 1 is the first response, 2 is the seconds response, etc. If null then startIndex is
	 *            used.
	 * @param format
	 *            The format of the response body
	 * @param body
	 *            The response body
	 */
	@Given("^web service \"(\\S.+\\S)\" responses? (\\d+)(?: thru (\\d+))? body is this (JSON|XML|text|binary):$")
	public void webServiceRespondsWithBody(String webServiceName, int startIndex, Integer endIndex, DataFormat format, String body) {

		byte[] bodyBytes = format.textToBytes(body);
		WebServiceConfig config = getOrCreateWebServiceConfig(webServiceName);
		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {
			HttpResponseData responseData = config.getResponseData(i);
			responseData.setBody(bodyBytes);
		}
	}

	/**
	 * Configures a response header for the specified mock web service.
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param startIndex
	 *            The index of the first response in the range to configure: 1 is the first response, 2 is the seconds response, etc
	 * @param endIndex
	 *            The index of the last response in the range to configure: 1 is the first response, 2 is the seconds response, etc. If null then startIndex is
	 *            used.
	 * @param headerName
	 *            The name of the header
	 * @param headerValue
	 *            The value of the header
	 */
	@Given("^web service \"(\\S.+\\S)\" responses? (\\d+)(?: thru (\\d+))? header \"(\\S+)\" is \"(\\S+)\"$")
	public void webServiceRespondsWithHeader(String webServiceName, int startIndex, Integer endIndex, String headerName, String headerValue) {

		WebServiceConfig config = getOrCreateWebServiceConfig(webServiceName);
		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {
			HttpResponseData responseData = config.getResponseData(i);
			responseData.setHeader(headerName, headerValue);
		}
	}

	/**
	 * Configures multiple response headers for the specified mock web service. The values are in a table with "Name" and "Value" columns like this:<code><pre>
	 * Given web service "foo" response 1 uses these headers:
	 * | Name | Value |
	 * | header1 | value1 |
	 * | header2 | value2 |
	 * ...
	 * </pre></code>
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param startIndex
	 *            The index of the first response in the range to configure: 1 is the first response, 2 is the seconds response, etc
	 * @param endIndex
	 *            The index of the last response in the range to configure: 1 is the first response, 2 is the seconds response, etc. If null then startIndex is
	 *            used.
	 * @param headers
	 *            List of the headers to include in the response
	 */
	@Given("^web service \"(\\S.+\\S)\" responses? (\\d+)(?: thru (\\d+))? uses? these headers:$")
	public void webServiceRespondsWithHeaders(String webServiceName, int startIndex, Integer endIndex, List<NameValuePair> headers) {

		WebServiceConfig config = getOrCreateWebServiceConfig(webServiceName);
		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {
			HttpResponseData responseData = config.getResponseData(i);
			for (NameValuePair header : headers) {
				responseData.setHeader(header);
			}
		}
	}

	/**
	 * Configures the HTTP status code sent in the mock web service response.
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param startIndex
	 *            The index of the first response in the range to configure: 1 is the first response, 2 is the seconds response, etc
	 * @param endIndex
	 *            The index of the last response in the range to configure: 1 is the first response, 2 is the seconds response, etc. If null then startIndex is
	 *            used.
	 * @param statusCode
	 *            The HTTP status code for the response
	 */
	@Given("^web service \"(\\S.+\\S)\" responses? (\\d+)(?: thru (\\d+))? uses? status code (\\d{3})$")
	public void webServiceRespondsWithStatusCode(String webServiceName, int startIndex, Integer endIndex, int statusCode) {

		WebServiceConfig config = getOrCreateWebServiceConfig(webServiceName);
		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {
			HttpResponseData responseData = config.getResponseData(i);
			responseData.setStatusCode(statusCode);
		}
	}

	/**
	 * Validates a mock web service request body
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param startIndex
	 *            The index of the first response in the range to configure: 1 is the first response, 2 is the seconds response, etc
	 * @param endIndex
	 *            The index of the last response in the range to configure: 1 is the first response, 2 is the seconds response, etc. If null then startIndex is
	 *            used.
	 * @param comparison
	 *            The method used to compare the specified body with the actual request body:
	 *            <ul>
	 *            <li>be: The bodies are compared for equality</li>
	 *            <li>matches: The request body must match the specified regular expression</li>
	 *            </ul>
	 * @param format
	 *            The format the body is in. For binary this should be space delimited hexadecimal.
	 * @param bodyRegex
	 *            A regular expression which the request body must match. If the format is "binary" then the body is hexadecimal text. If the body contains
	 *            multiple lines each line will have all leading and trailing whitespace removed then all lines will be concatenated into a single line.
	 */
	@Then("^web service \"(\\S.+\\S)\" requests? (\\d+)(?: thru (\\d+))? body should match this (JSON|XML|text|binary):$")
	public void webServiceRequestBodyShouldBe(String webServiceName, int startIndex, Integer endIndex, DataFormat format, String bodyRegex) {

		WebServiceConfig config = getRequiredWebServiceConfig(webServiceName);

		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {
			HttpRequestData requestData = config.getRequestData(i);
			String bodyText = format.bytesToText(requestData.getBody());
			bodyRegex = format.normalizeRegex(bodyRegex);
			assertThat(bodyText, matches(bodyRegex), "Web service %s request %d body does not match", webServiceName, i);
		}
	}

	/**
	 * Validates the headers received in a request to the specified mock web service. The values are in a table with "Name" and "Value" columns like this:
	 * <code><pre>
	 * Then web service "foo" request 1 headers should include:
	 * | Name | Value |
	 * | header1 | value1 |
	 * | header2 | value2 |
	 * ...
	 * </pre></code>
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param startIndex
	 *            The index of the first response in the range to configure: 1 is the first response, 2 is the seconds response, etc
	 * @param endIndex
	 *            The index of the last response in the range to configure: 1 is the first response, 2 is the seconds response, etc. If null then startIndex is
	 *            used.
	 * @param headerMatcher
	 *            The method used to compare the specified headers with the actual request headers:
	 *            <ul>
	 *            <li>be: All the headers in the request must be equal to all the specified headers. Any headers in the request not specified or vice-versa will
	 *            fail the comparison.</li>
	 *            <li>include: All the headers specified must equal headers in the request but any headers in the request not specified are ignored.</li>
	 *            </ul>
	 * @param headers
	 *            The headers to compare
	 */
	@Then("^web service \"(\\S.+\\S)\" requests? (\\d+)(?: thru (\\d+))? headers should (be|include):$")
	public void webServiceRequestHeadersShouldBe(String webServiceName, int startIndex, Integer endIndex, TableMatcher headerMatcher,
			List<NameValuePair> headers) {

		WebServiceConfig config = getRequiredWebServiceConfig(webServiceName);
		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {
			HttpRequestData requestData = config.getRequestData(i);
			assertThat(requestData.getHeaders(), headerMatcher.newMatcher(headers), "Web service %s request %d headers do not match", webServiceName, i);
		}
	}

	/**
	 * Validates a single header received in a request to the specified mock web service.
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param startIndex
	 *            The index of the first response in the range to configure: 1 is the first response, 2 is the seconds response, etc
	 * @param endIndex
	 *            The index of the last response in the range to configure: 1 is the first response, 2 is the seconds response, etc. If null then startIndex is
	 *            used.
	 * @param comparison
	 *            The method used to compare the specified header value with the actual request header value:
	 *            <ul>
	 *            <li>be: The values are compared for equality</li>
	 *            <li>matches: The request header value must match the specified regular expression</li>
	 *            </ul>
	 * @param headerName
	 *            The name of the header to compare
	 * @param headerValueRegex
	 *            A regular expression the header value must match
	 */
	@Then("^web service \"(\\S.+\\S)\" requests? (\\d+)(?: thru (\\d+))? header \"(\\S+)\" should match \"(\\S+)\"$")
	public void webServiceRequestHeaderShouldBe(String webServiceName, int startIndex, Integer endIndex, String headerName, String headerValueRegex) {

		WebServiceConfig config = getRequiredWebServiceConfig(webServiceName);
		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {
			HttpRequestData requestData = config.getRequestData(i);
			assertThat(requestData.getHeaderValue(headerName), matches(headerValueRegex), "Web service %s request %d header %s does not match", webServiceName,
					i, headerName);
		}
	}

	/**
	 * Validates the HTTP method used in a request to the specified mock web service
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param startIndex
	 *            The index of the first response in the range to configure: 1 is the first response, 2 is the seconds response, etc
	 * @param endIndex
	 *            The index of the last response in the range to configure: 1 is the first response, 2 is the seconds response, etc. If null then startIndex is
	 *            used.
	 * @param method
	 *            The HTTP method the request should have used
	 */
	@Then("^web service \"(\\S.+\\S)\" requests? (\\d+)(?: thru (\\d+))? method should be \"(GET|POST|HEAD|PUT|OPTIONS|DELETE|TRACE|CONNECT|MOVE)\"$")
	public void webServiceRequestMethodShouldBe(String webServiceName, int startIndex, Integer endIndex, HttpMethod method) {

		WebServiceConfig config = getRequiredWebServiceConfig(webServiceName);
		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {
			HttpRequestData requestData = config.getRequestData(i);
			assertThat(requestData.getRequestMethod(), equalTo(method), "Web service %s request %d request method does not match", webServiceName, i);
		}
	}

	/**
	 * Validates the number of requests received by the specified web service.
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param requestCountQuantifier
	 *            How to evaluate the request count
	 * @param requestCount
	 *            The number of requests to match
	 */
	@Then("^web service \"(\\S.+\\S)\" should have (at least|at most|exactly) (\\d+) requests?$")
	public void webServiceShouldHaveRequestCount(String webServiceName, @Transform(CountQuantifierTransformer.class) CountQuantifier requestCountQuantifier,
			int requestCount) {

		final WebServiceConfig config = getRequiredWebServiceConfig(webServiceName);
		assertThat(config.getRequestCount(), requestCountQuantifier.newMatcher(requestCount), "Web service %s request counts do not match", webServiceName);
	}

	/**
	 * Validates the number of requests received by the specified web service within a specified time.
	 * 
	 * @param webServiceName
	 *            The name used to refer to this web service in the DSL
	 * @param requestCount
	 *            The number of requests to match
	 * @param timeout
	 *            Max time to wait for the condition to be true
	 * @param timeoutUnit
	 *            Unit of measure for the timeout (seconds or milliseconds)
	 */
	@Then("^web service \"(\\S.+\\S)\" should have at least (\\d+) requests? within (\\d+) (seconds|milliseconds)$")
	public void webServiceShouldHaveRequestCountWithin(String webServiceName, int requestCount, int timeout, TimeUnit timeoutUnit) {

		final WebServiceConfig config = getRequiredWebServiceConfig(webServiceName);

		Callable<Integer> currentResponseCount = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return config.getRequestCount();
			}
		};
		assertThatWithin(timeout, timeoutUnit, currentResponseCount, gte(requestCount), "Web service %s did not receive enough requests", webServiceName);
	}

	private WebServiceConfig getOrCreateWebServiceConfig(String webServiceName) {
		WebServiceConfig config = webServiceConfigByName.get(webServiceName);
		if (config == null) {
			config = new WebServiceConfig();
			webServiceConfigByName.put(webServiceName, config);
		}

		return config;
	}

	private WebServiceConfig getRequiredWebServiceConfig(String webServiceName) {

		WebServiceConfig config = webServiceConfigByName.get(webServiceName);
		if (config == null) {
			throw new RelishException("Web service %s does not exist", webServiceName);
		}

		return config;
	}

	private int getEndIndex(int startIndex, Integer endIndex) {
		return endIndex == null ? startIndex : endIndex;
	}
}
