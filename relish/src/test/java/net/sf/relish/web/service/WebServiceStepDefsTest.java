package net.sf.relish.web.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import net.sf.relish.rule.ElapsedTime;
import net.sf.relish.RelishUtil;
import net.sf.relish.CountQuantifier;
import net.sf.relish.DataFormat;
import net.sf.relish.NameValuePair;
import net.sf.relish.RelishException;
import net.sf.relish.TableMatcher;
import net.sf.relish.web.AbstractWebServerTest;
import net.sf.relish.web.HttpMethod;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebServiceStepDefsTest {

	@Rule public final ExpectedException expectedException = ExpectedException.none();
	@Rule public final ElapsedTime elapsedTime = new ElapsedTime();

	WebServiceStepDefs steps = new WebServiceStepDefs();
	List<NameValuePair> headers = Arrays.asList(new NameValuePair("abc", "123"), new NameValuePair("def", "456"));

	@After
	public void after() throws Exception {
		steps.after();
	}

	@Test
	public void testAfter_ServerNotRunning() throws Exception {

		steps.after();
	}

	@Test
	public void testAfter_ServerRunning() throws Exception {

		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		assertTrue(isServerRunning());
		steps.after();
		assertFalse(isServerRunning());
	}

	@Test
	public void testWebServiceIsRunningAt_ResponseNotConfigured() throws Exception {

		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		assertEquals(503, conn.getResponseCode());
	}

	@Test
	public void testWebServiceIsRunningAt_ResponseConfigured() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 1, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");

		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
	}

	@Test
	public void testWebServiceIsRunningAt_ServiceAlreadyRunning() throws Exception {

		expectedException.expect(RelishException.class);
		expectedException.expectMessage("You cannot start web service foo because it is already running");

		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
	}

	@Test
	public void testWebServiceIsStopped_ServiceDoesNotExist() throws Exception {

		expectedException.expect(RelishException.class);
		expectedException.expectMessage("Web service foo does not exist");

		steps.webServiceIsStopped("foo");
	}

	@Test
	public void testWebServiceIsStopped_ServiceNotRunning() throws Exception {

		expectedException.expect(RelishException.class);
		expectedException.expectMessage("You cannot stop web service foo because it is not running");

		steps.webServiceIsRunningAt("foo", 12473, "foo");
		steps.webServiceIsStopped("foo");
		steps.webServiceIsStopped("foo");
	}

	@Test
	public void testWebServiceIsStopped_ServiceRunning() throws Exception {

		steps.webServiceIsRunningAt("foo", 12473, "foo");
		steps.webServiceIsStopped("foo");

		HttpURLConnection conn = newConnection();
		assertEquals(404, conn.getResponseCode());
	}

	@Test
	public void testWebServiceRespondsWithBody() throws Exception {

		steps.webServiceRespondsWithBody("foo", 1, 1, DataFormat.TEXT, "hello world");
		steps.webServiceIsRunningAt("foo", 12473, "/foo");

		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		assertEquals("hello world", getResponseBody(conn));
	}

	@Test
	public void testWebServiceRespondsWithHeader() throws Exception {

		steps.webServiceRespondsWithHeader("foo", 1, 1, "abc", "123");
		steps.webServiceIsRunningAt("foo", 12473, "/foo");

		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		assertEquals("123", conn.getHeaderField("abc"));
	}

	@Test
	public void testWebServiceRespondsWithHeaders() throws Exception {

		steps.webServiceRespondsWithHeaders("foo", 1, 1, headers);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");

		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		assertEquals("123", conn.getHeaderField("abc"));
		assertEquals("456", conn.getHeaderField("def"));
	}

	@Test
	public void testWebServiceRespondsWithStatusCode() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 1, 201);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");

		HttpURLConnection conn = newConnection();
		assertEquals(201, conn.getResponseCode());
	}

	@Test
	public void testWebServiceRequestBodyShouldBe() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 1, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		conn.setDoOutput(true);
		new PrintStream(conn.getOutputStream()).print("hello world");
		conn.connect();
		assertEquals(200, conn.getResponseCode());
		steps.webServiceRequestBodyShouldBe("foo", 1, 1, DataFormat.TEXT, "hello world");
	}

	@Test
	public void testWebServiceRequestHeadersShouldBe_Success() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 1, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		conn.addRequestProperty("abc", "123");
		conn.addRequestProperty("def", "456");
		conn.connect();
		assertEquals(200, conn.getResponseCode());
		steps.webServiceRequestHeadersShouldBe("foo", 1, 1, TableMatcher.INCLUDE, headers);
	}

	@Test(expected = AssertionError.class)
	public void testWebServiceRequestHeadersShouldBe_Fail() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 1, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		steps.webServiceRequestHeadersShouldBe("foo", 1, 1, TableMatcher.INCLUDE, headers);
	}

	@Test
	public void testWebServiceRequestHeaderShouldBe_Success() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 1, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		conn.addRequestProperty("abc", "123");
		conn.connect();
		assertEquals(200, conn.getResponseCode());
		steps.webServiceRequestHeaderShouldBe("foo", 1, 1, "abc", "123");
	}

	@Test(expected = AssertionError.class)
	public void testWebServiceRequestHeaderShouldBe_Fail() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 1, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		steps.webServiceRequestHeaderShouldBe("foo", 1, 1, "abc", "123");
	}

	@Test
	public void testWebServiceRequestMethodShouldBe_Success() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 1, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		steps.webServiceRequestMethodShouldBe("foo", 1, 1, HttpMethod.GET);
	}

	@Test(expected = AssertionError.class)
	public void testWebServiceRequestMethodShouldBe_Fail() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 1, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		steps.webServiceRequestMethodShouldBe("foo", 1, 1, HttpMethod.PUT);
	}

	@Test
	public void testWebServiceShouldHaveRequestCount_Success() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 2, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		steps.webServiceShouldHaveRequestCount("foo", CountQuantifier.EXACTLY, 2);
	}

	@Test(expected = AssertionError.class)
	public void testWebServiceShouldHaveRequestCount_Fail() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 2, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		steps.webServiceShouldHaveRequestCount("foo", CountQuantifier.EXACTLY, 2);
	}

	@Test(expected = AssertionError.class)
	public void testWebServiceShouldHaveRequestCountWithin_TimesOut() throws Exception {

		elapsedTime.expectMinMillis(900);
		elapsedTime.expectMaxMillis(1500);

		steps.webServiceIsRunningAt("foo", 12473, "/foo");

		steps.webServiceShouldHaveRequestCountWithin("foo", 2, 1, TimeUnit.SECONDS);
	}

	@Test
	public void testWebServiceShouldHaveRequestCountWithin_Success() throws Exception {

		steps.webServiceRespondsWithStatusCode("foo", 1, 2, 200);
		steps.webServiceIsRunningAt("foo", 12473, "/foo");
		HttpURLConnection conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		conn = newConnection();
		assertEquals(200, conn.getResponseCode());
		steps.webServiceShouldHaveRequestCountWithin("foo", 1, 1, TimeUnit.SECONDS);
	}

	private HttpURLConnection newConnection() throws Exception {

		return (HttpURLConnection) new URL("http://localhost:12473/foo").openConnection();
	}

	private boolean isServerRunning() throws Exception {
		return AbstractWebServerTest.isServerRunning("http://localhost:12473/foo");
	}

	private String getResponseBody(HttpURLConnection conn) {

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

		return new String(body);
	}
}
