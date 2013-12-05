package net.sf.relish.web;

import static org.junit.Assert.*;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.jetty.server.Handler;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractWebServerTest {

	@Mock Handler handler;
	TestServer server = new TestServer();

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void after() throws Exception {
		if (server != null) {
			server.stopServer();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStartServer_ServerPortTooLow() throws Exception {
		server.startServer(0, handler);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStartServer_ServerPortTooHigh() throws Exception {
		server.startServer(0x10000, handler);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStartServer_HandlerIsNull() throws Exception {
		server.startServer(12473, null);
	}

	@Test
	public void testStartServer() throws Exception {
		server.startServer(12473, handler);
		assertTrue(isServerRunning());
		assertTrue(server.isRunning());
	}

	@Test
	public void testStopServer_ServerNotRunning() throws Exception {
		server.stopServer();
	}

	@Test
	public void testStopServer_ServerRunning() throws Exception {
		server.startServer(12473, handler);
		assertTrue(isServerRunning());
		server.stopServer();
		assertFalse(isServerRunning());
	}

	@Test
	public void testRestartServer() throws Exception {
		server.startServer(12473, handler);
		assertTrue(isServerRunning());

		server.stopServer();
		assertFalse(isServerRunning());

		server.startServer(12473, handler);
		assertTrue(isServerRunning());

		server.stopServer();
		assertFalse(isServerRunning());
	}

	@Test
	public void testIsServerRunning() throws Exception {
		assertFalse(server.isRunning());
		server.startServer(12473, handler);
		assertTrue(server.isRunning());
		assertTrue(isServerRunning());
		server.stopServer();
		assertFalse(server.isRunning());
		assertFalse(isServerRunning());
	}

	/**
	 * @return true if there is a web server running at the specified url
	 */
	public static boolean isServerRunning(String url) throws Exception {

		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.connect();
			conn.disconnect();
			return true;
		} catch (ConnectException e) {
			return false;
		}
	}

	private boolean isServerRunning() throws Exception {
		return isServerRunning("http://localhost:12473/foo");
	}

	private static final class TestServer extends AbstractWebServer {

	}
}
