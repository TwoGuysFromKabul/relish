package net.sf.relish.web.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import net.sf.relish.RelishException;
import net.sf.relish.web.HttpMethod;
import net.sf.relish.web.HttpRequestData;
import net.sf.relish.web.HttpResponseData;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebServiceConfigTest {

	@Mock HttpServletRequest request;
	WebServiceConfig config = new WebServiceConfig();
	Hashtable<String, String> headers = new Hashtable<String, String>();

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(request.getHeaderNames()).thenReturn(headers.keys());
		when(request.getMethod()).thenReturn("GET");
		when(request.getRequestURL()).thenReturn(new StringBuffer("http://www.foo.com/abc"));
		when(request.getHeader(anyString())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String key = (String) invocation.getArguments()[0];
				return headers.get(key);
			}
		});
	}

	@Test
	public void testEnable_AlreadyEnabled() {

		assertTrue(config.enable("foo"));
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE_503, config.handleRequest(request).getStatusCode());
		assertFalse(config.enable("foo"));
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE_503, config.handleRequest(request).getStatusCode());
	}

	@Test
	public void testEnable() {
		assertTrue(config.enable("foo"));
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE_503, config.handleRequest(request).getStatusCode());
	}

	@Test
	public void testDisable_AlreadyDisabled() {

		assertFalse(config.disable());
		assertEquals(HttpStatus.NOT_FOUND_404, config.handleRequest(request).getStatusCode());
	}

	@Test
	public void testDisable() {

		config.enable("/foo");
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE_503, config.handleRequest(request).getStatusCode());

		assertTrue(config.disable());
		assertEquals(HttpStatus.NOT_FOUND_404, config.handleRequest(request).getStatusCode());
	}

	@Test
	public void testHandleRequest_ServerNotEnabled() throws Exception {

		assertEquals(HttpStatus.NOT_FOUND_404, config.handleRequest(request).getStatusCode());
	}

	@Test
	public void testHandleRequest_ResponseNotConfigured() throws Exception {

		config.enable("/foo");
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE_503, config.handleRequest(request).getStatusCode());
	}

	@Test
	public void testHandleRequest_NoHeadersNoBody_Success() throws Exception {

		config.enable("/foo");

		HttpResponseData responseData1 = config.getResponseData(1);
		responseData1.setStatusCode(200);
		HttpResponseData responseData3 = config.getResponseData(3);
		responseData3.setStatusCode(200);

		assertSame(responseData1, config.handleRequest(request));
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE_503, config.handleRequest(request).getStatusCode());
		assertSame(responseData3, config.handleRequest(request));

		assertEquals(3, config.getRequestCount());

		for (int i = 1; i <= 3; i++) {
			HttpRequestData data = config.getRequestData(i);
			assertNull(data.getBody());
			assertTrue(data.getHeaders().isEmpty());
			assertEquals(HttpMethod.GET, data.getRequestMethod());
			assertEquals("http://www.foo.com/abc", data.getUrl());
		}
	}

	@Test
	public void testHandleRequest_WithHeadersAndBody_Success() throws Exception {

		headers.put("foo", "bar");
		headers.put("abc", "123");
		when(request.getHeaderNames()).thenReturn(headers.keys());
		when(request.getInputStream()).thenReturn(new TestInputStream());

		config.enable("/foo");

		HttpResponseData responseData = config.getResponseData(1);
		assertSame(responseData, config.handleRequest(request));

		assertEquals(1, config.getRequestCount());

		HttpRequestData data = config.getRequestData(1);
		assertArrayEquals(new byte[] { 1, 2, 3 }, data.getBody());
		assertEquals(2, data.getHeaders().size());
		assertEquals("bar", data.getHeaderValue("foo"));
		assertEquals("123", data.getHeaderValue("abc"));
		assertEquals(HttpMethod.GET, data.getRequestMethod());
		assertEquals("http://www.foo.com/abc", data.getUrl());
	}

	@Test
	public void testGetTarget() throws Exception {

		assertEquals("", config.getTarget());
		config.enable("/foo");
		assertEquals("/foo", config.getTarget());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetRequestData_IndexIsZero() throws Exception {

		config.getRequestData(0);
	}

	@Test(expected = RelishException.class)
	public void testGetRequestData_RequestDoesNotExist() throws Exception {

		config.getRequestData(1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetResponseData_IndexIsZero() throws Exception {

		config.getResponseData(0);
	}

	@Test
	public void assertAllPublicMethodsSynchronized() {

		for (Method method : WebServiceConfig.class.getMethods()) {
			if (method.getDeclaringClass() != Object.class) {
				assertTrue("Method is not synchronized: " + method, Modifier.isSynchronized(method.getModifiers()));
			}
		}
	}

	private static class TestInputStream extends ServletInputStream {

		ByteArrayInputStream in = new ByteArrayInputStream(new byte[] { 1, 2, 3 });

		@Override
		public int read() throws IOException {
			return in.read();
		}
	}
}
