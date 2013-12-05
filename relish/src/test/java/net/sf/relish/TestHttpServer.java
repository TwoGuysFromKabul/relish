package net.sf.relish;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

/**
 * An HTTP server for unit testing. A Jetty server is started on construction and stopped when {@link #shutdown()} is invoked. All requests are passed to
 * {@link #handle(String, HttpServletRequest, HttpServletResponse, int)} which should be implemented by an extending class for the test.
 */
public class TestHttpServer {

	private final Server server;
	private final int port;

	private HttpServletRequest currentRequest;
	private HttpServletResponse currentResponse;

	private volatile String lastTarget;
	private volatile byte[] lastRequestBody;
	private final Map<String, String> lastRequestHeaders = new ConcurrentHashMap<String, String>();

	private volatile byte[] responseBodyToSend;
	private volatile int responseStatusToSend = 200;
	private final Map<String, String> responseHeadersToSend = new ConcurrentHashMap<String, String>();

	/**
	 * Creates a server on an ephemeral port.
	 */
	public TestHttpServer() {

		try {
			this.server = new Server(0);
			server.setHandler(new RequestHandler());
			server.start();
			Connector[] connectors = server.getConnectors();
			SelectChannelConnector connector = (SelectChannelConnector) connectors[0];
			this.port = connector.getLocalPort();
		} catch (Exception e) {
			throw new RuntimeException("Failed to start jetty server", e);
		}
	}

	/**
	 * Shuts down the server. Blocks until shutdown is complete;
	 */
	public final void shutdown() throws Exception {

		server.stop();
		server.join();
	}

	/**
	 * @return The base URL to this server: http://localhost:port/
	 */
	public final String getBaseURL() {

		return String.format("http://localhost:%d/", port);
	}

	/**
	 * @return The target param from the last invocation of {@link #handle(String, HttpServletRequest, HttpServletResponse, int)}.
	 */
	public final String getTarget() {

		return lastTarget;
	}

	/**
	 * @return The named header from the last request. Null if there is no header with the specified name.
	 */
	public final String getRequestHeader(String name) {

		return lastRequestHeaders.get(name);
	}

	/**
	 * @return The body from the last request as a string with charset UTF-8.
	 */
	public final String getRequestBodyString() {

		return new String(lastRequestBody, DataFormat.UTF8);
	}

	/**
	 * Sets the HTTP status for future responses.
	 */
	public final void setResponseStatus(int responseStatusToSend) {

		this.responseStatusToSend = responseStatusToSend;
	}

	/**
	 * Sets the header value for future responses.
	 */
	public final void setResponseHeader(String name, String value) {

		responseHeadersToSend.put(name, value);
	}

	/**
	 * Sets the body for future responses as a string using the UTF-8 charset.
	 */
	public final void setResponseBody(String body) {

		this.responseBodyToSend = body.getBytes(DataFormat.UTF8);
	}

	/**
	 * Handles an HTTP request. This implementation saves the request target, headers, and body to properties on this object and sends the configured response
	 * headers, body, and status. May be overridden if other request processing is required.
	 * 
	 * @param target
	 *            The target of the request - either a URI or a name.
	 * @param request
	 *            The request either as the {@link Request} object or a wrapper of that request. The {@link HttpConnection#getCurrentConnection()} method can be
	 *            used access the Request object if required.
	 * @param response
	 *            The response as the {@link Response} object or a wrapper of that response. The {@link HttpConnection#getCurrentConnection()} method can be
	 *            used access the Response object if required.
	 * @param dispatch
	 *            The dispatch mode: {@link Handler#REQUEST}, {@link Handler##FORWARD}, {@link Handler##INCLUDE}, {@link Handler##ERROR}
	 * 
	 * @return The HTTP status code to return. 500 is returned if an exception is thrown.
	 */
	protected int handle(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {

		lastRequestBody = null;
		lastRequestHeaders.clear();
		lastTarget = target;

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			String value = request.getHeader(name);
			lastRequestHeaders.put(name, value);
		}
		lastRequestBody = readRequestBodyBytes();

		for (Map.Entry<String, String> entry : responseHeadersToSend.entrySet()) {
			if (entry.getValue() != null) {
				response.setHeader(entry.getKey(), entry.getValue());
			}
		}
		if (responseBodyToSend != null && responseBodyToSend.length > 0) {
			writeResponseBody(responseBodyToSend);
		}

		return responseStatusToSend;
	}

	/**
	 * Reads the request body as a byte[]. Must be called from the thread calling {@link #handle(String, HttpServletRequest, HttpServletResponse, int)}.
	 */
	protected final byte[] readRequestBodyBytes() {

		try {
			InputStream in = currentRequest.getInputStream();
			if (in == null) {
				return null;
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			for (int i = in.read(); i >= 0; i = in.read()) {
				out.write(i);
			}
			return out.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Error while reading request", e);
		}
	}

	/**
	 * Reads the request body as a string with charset UTF-8. Must be called from the thread calling
	 * {@link #handle(String, HttpServletRequest, HttpServletResponse, int)}.
	 */
	protected final String readRequestBodyString() {

		return readRequestBodyString("UTF-8");
	}

	/**
	 * Reads the request body as a string with the specified charset. Must be called from the thread calling
	 * {@link #handle(String, HttpServletRequest, HttpServletResponse, int)}.
	 */
	protected final String readRequestBodyString(String charset) {

		try {
			return new String(readRequestBodyBytes(), charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error while reading request", e);
		}
	}

	/**
	 * Writes the response body for the current request as a byte[]. Must be called from the thread calling
	 * {@link #handle(String, HttpServletRequest, HttpServletResponse, int)}.
	 */
	protected final void writeResponseBody(byte[] body) {

		try {
			OutputStream out = currentResponse.getOutputStream();
			out.write(body);
		} catch (Exception e) {
			throw new RuntimeException("Error while writing response", e);
		}
	}

	/**
	 * Writes the response body for the current request as a string using the specified charset. Must be called from the thread calling
	 * {@link #handle(String, HttpServletRequest, HttpServletResponse, int)}.
	 */
	protected final void writeResponseBody(String body, String charset) {

		try {
			writeResponseBody(body.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error while writing response", e);
		}
	}

	/**
	 * Writes the response body for the current request as a string using the UTF-8 charset. Must be called from the thread calling
	 * {@link #handle(String, HttpServletRequest, HttpServletResponse, int)}.
	 */
	protected final void writeResponseBody(String body) {

		writeResponseBody(body, "UTF-8");
	}

	private class RequestHandler extends AbstractHandler {

		/**
		 * @see org.mortbay.jetty.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
		 */
		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

			int responseCode = 500;
			currentRequest = request;
			currentResponse = response;
			try {
				responseCode = TestHttpServer.this.handle(target, request, response);
			} catch (ServletException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException("Exception while handling request", e);
			} finally {
				currentRequest = null;
				currentResponse = null;
				((Request) request).setHandled(true);
				response.setStatus(responseCode);
			}
		}
	}
}
