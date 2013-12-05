package net.sf.relish.web.client;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import net.sf.relish.CountQuantifier;
import net.sf.relish.DataFormat;
import net.sf.relish.NameValuePair;
import net.sf.relish.TableMatcher;
import net.sf.relish.TestHttpServer;
import net.sf.relish.web.HttpMethod;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebClientStepDefsTest {

	TestHttpServer server = new TestHttpServer();
	WebClientStepDefs steps = new WebClientStepDefs();
	List<NameValuePair> headers = Arrays.asList(new NameValuePair[] { new NameValuePair("foo", "bar"), new NameValuePair("abc", "123") });

	@Test
	public void testWebClientRequestBodyIs() throws Exception {

		steps.webClientRequestBodyIs(1, 1, DataFormat.TEXT, "hello world");
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);

		assertEquals("hello world", server.getRequestBodyString());
	}

	@Test
	public void testWebClientRequestHeaderIs() throws Exception {

		steps.webClientRequestHeaderIs(1, 1, "foo", "bar");
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);

		assertEquals("bar", server.getRequestHeader("foo"));
	}

	@Test
	public void testWebClientRequestUsesHeaders() throws Exception {

		steps.webClientRequestUsesHeaders(1, 1, headers);
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);

		assertEquals("bar", server.getRequestHeader("foo"));
		assertEquals("123", server.getRequestHeader("abc"));
	}

	@Test
	public void testWebClientRequestIsSentTo_Success() throws Exception {

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL() + "fubar", HttpMethod.GET);

		assertEquals("/fubar", server.getTarget());
	}

	@Test
	public void testWebClientRequestIsSentTo_ServerReturnsError_NoBody() throws Exception {

		server.setResponseStatus(500);
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL() + "fubar", HttpMethod.GET);

		assertEquals("/fubar", server.getTarget());
	}

	@Test
	public void testWebClientResponseBodyShouldBe_ServerReturnsError() throws Exception {

		server.setResponseStatus(500);
		server.setResponseBody("hello world");
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL() + "fubar", HttpMethod.GET);
		steps.webClientResponseBodyShouldBe(1, 1, DataFormat.TEXT, "hello world");
	}

	@Test
	public void testWebClientResponseBodyShouldBe_ServerReturnsSuccess() throws Exception {

		server.setResponseBody("hello world");
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL() + "fubar", HttpMethod.GET);
		steps.webClientResponseBodyShouldBe(1, 1, DataFormat.TEXT, "hello world");
	}

	@Test
	public void testWebClientResponseHeadersShouldBe_BE_Success() throws Exception {

		server.setResponseHeader("foo", "bar");
		server.setResponseHeader("abc", "123");
		server.setResponseHeader("Content-Length", "0");
		server.setResponseHeader("Server", "test server");
		headers = Arrays.asList(new NameValuePair[] { new NameValuePair("Content-Length", "0"), new NameValuePair("Server", "test server"),
				new NameValuePair("foo", "bar"), new NameValuePair("abc", "123") });

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientResponseHeadersShouldBe(1, 1, TableMatcher.BE, headers);
	}

	@Test(expected = AssertionError.class)
	public void testWebClientResponseHeadersShouldBe_BE_Fail_ResponseHasUnspecifiedHeaders() throws Exception {
		server.setResponseHeader("foo", "bar");
		server.setResponseHeader("abc", "123");
		server.setResponseHeader("Content-Length", "0");
		server.setResponseHeader("Server", "test server");
		headers = Arrays.asList(new NameValuePair[] { new NameValuePair("Content-Length", "0"), new NameValuePair("Server", "test server"),
				new NameValuePair("abc", "123") });

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientResponseHeadersShouldBe(1, 1, TableMatcher.BE, headers);
	}

	@Test(expected = AssertionError.class)
	public void testWebClientResponseHeadersShouldBe_BE_Fail_ResponseIsMissingSpecifiedHeaders() throws Exception {
		server.setResponseHeader("abc", "123");
		server.setResponseHeader("Content-Length", "0");
		server.setResponseHeader("Server", "test server");
		headers = Arrays.asList(new NameValuePair[] { new NameValuePair("Content-Length", "0"), new NameValuePair("Server", "test server"),
				new NameValuePair("foo", "bar"), new NameValuePair("abc", "123") });

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientResponseHeadersShouldBe(1, 1, TableMatcher.BE, headers);
	}

	@Test
	public void testWebClientResponseHeaderShouldBe_INCLUDE_Success_HeadersAllMatch() throws Exception {

		server.setResponseHeader("foo", "bar");
		server.setResponseHeader("abc", "123");
		server.setResponseHeader("Content-Length", "0");
		server.setResponseHeader("Server", "test server");
		headers = Arrays.asList(new NameValuePair[] { new NameValuePair("Content-Length", "0"), new NameValuePair("Server", "test server"),
				new NameValuePair("foo", "bar"), new NameValuePair("abc", "123") });

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientResponseHeadersShouldBe(1, 1, TableMatcher.BE, headers);
	}

	@Test
	public void testWebClientResponseHeaderShouldBe_INCLUDE_Success_ResponseIncludesUnspecifiedHeader() throws Exception {

		server.setResponseHeader("foo", "bar");
		server.setResponseHeader("abc", "123");
		server.setResponseHeader("Content-Length", "0");
		server.setResponseHeader("Server", "test server");
		headers = Arrays.asList(new NameValuePair[] { new NameValuePair("Content-Length", "0"), new NameValuePair("Server", "test server"),
				new NameValuePair("abc", "123") });

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientResponseHeadersShouldBe(1, 1, TableMatcher.INCLUDE, headers);
	}

	@Test(expected = AssertionError.class)
	public void testWebClientResponseHeaderShouldBe_INCLUDE_Fail_ResponseIsMissingSpecifiedHeaders() throws Exception {
		server.setResponseHeader("abc", "123");
		server.setResponseHeader("Content-Length", "0");
		server.setResponseHeader("Server", "test server");
		headers = Arrays.asList(new NameValuePair[] { new NameValuePair("Content-Length", "0"), new NameValuePair("Server", "test server"),
				new NameValuePair("foo", "bar"), new NameValuePair("abc", "123") });

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientResponseHeadersShouldBe(1, 1, TableMatcher.INCLUDE, headers);
	}

	@Test
	public void testWebClientResponseStatusCodeShouldBe_Success() throws Exception {

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientResponseStatusCodeShouldBe(1, 1, 200);
	}

	@Test(expected = AssertionError.class)
	public void testWebClientResponseStatusCodeShouldBe_Fail() throws Exception {
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientResponseStatusCodeShouldBe(1, 1, 201);
	}

	@Test
	public void testWebClientShouldHaveResponseCount_EXACTLY_Success() throws Exception {

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientShouldHaveResponseCount(CountQuantifier.EXACTLY, 2);
	}

	@Test
	public void testWebClientShouldHaveResponseCount_AT_LEAST_Success() throws Exception {

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientShouldHaveResponseCount(CountQuantifier.AT_LEAST, 1);
	}

	@Test
	public void testWebClientShouldHaveResponseCount_AT_MOST_Success() throws Exception {

		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientShouldHaveResponseCount(CountQuantifier.AT_MOST, 3);
	}

	@Test(expected = AssertionError.class)
	public void testWebClientShouldHaveResponseCount_Fail() throws Exception {
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientRequestIsSentTo(1, 1, server.getBaseURL(), HttpMethod.GET);
		steps.webClientShouldHaveResponseCount(CountQuantifier.EXACTLY, 1);
	}
}
