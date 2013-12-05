package net.sf.relish.web.service;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import net.sf.relish.web.HttpResponseData;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebServiceHandlerTest {

	@Mock Request baseRequest;
	@Mock HttpServletRequest request;
	@Mock HttpServletResponse response;

	Map<String, WebServiceConfig> webServiceConfigByName = new HashMap<String, WebServiceConfig>();
	WebServiceHandler handler = new WebServiceHandler(webServiceConfigByName);
	WebServiceConfig config = new WebServiceConfig();
	Hashtable<String, String> headers = new Hashtable<String, String>();
	TestOutputStream out = new TestOutputStream();

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		config.enable("/foo");
		webServiceConfigByName.put("/foo", config);

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
		when(response.getOutputStream()).thenReturn(out);
	}

	@Test
	public void testHandle_NoConfigForTarget() throws Exception {

		handler.handle("/abc", baseRequest, request, response);
		verify(response).setStatus(404);
		verifyZeroInteractions(baseRequest, request);
		verifyNoMoreInteractions(response);
	}

	@Test
	public void testHandle_ConfigNotEnabled() throws Exception {

		config.disable();

		handler.handle("/foo", baseRequest, request, response);
		verify(response).setStatus(404);
		verify(baseRequest).setHandled(true);
		verifyZeroInteractions(request);
		verifyNoMoreInteractions(response, baseRequest);
	}

	@Test
	public void testHandle_NoResponseBodyOrHeaders() throws Exception {

		HttpResponseData responseData = config.getResponseData(1);
		responseData.setStatusCode(200);

		handler.handle("/foo", baseRequest, request, response);

		verify(baseRequest).setHandled(true);
		verify(response).setStatus(200);
		verifyNoMoreInteractions(response, baseRequest);
	}

	@Test
	public void testHandle_WithResponseBodyAndHeaders() throws Exception {

		headers.put("foo", "bar");
		headers.put("abc", "123");
		when(request.getHeaderNames()).thenReturn(headers.keys());
		when(request.getInputStream()).thenReturn(new TestInputStream());

		HttpResponseData responseData = config.getResponseData(1);
		responseData.setBody(new byte[] { 4, 5, 6 });
		responseData.setHeader("responseName1", "789");
		responseData.setHeader("responseName2", "012");
		responseData.setStatusCode(200);

		handler.handle("/foo", baseRequest, request, response);

		verify(baseRequest).setHandled(true);
		verify(response).setStatus(200);
		verify(response).setHeader("responseName1", "789");
		verify(response).setHeader("responseName2", "012");
		verify(response).getOutputStream();
		verifyNoMoreInteractions(response, baseRequest);
	}

	private static class TestInputStream extends ServletInputStream {

		ByteArrayInputStream in = new ByteArrayInputStream(new byte[] { 1, 2, 3 });

		@Override
		public int read() throws IOException {
			return in.read();
		}
	}

	private static class TestOutputStream extends ServletOutputStream {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		@Override
		public void write(int b) throws IOException {
			out.write(b);
		}
	}
}
