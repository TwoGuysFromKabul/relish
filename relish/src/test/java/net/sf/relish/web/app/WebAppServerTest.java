package net.sf.relish.web.app;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sf.relish.web.AbstractWebServerTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebAppServerTest {

	WebAppServer server = new WebAppServer();

	@After
	public void after() throws Exception {
		if (server != null) {
			server.stopServer();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStartServer_ContextRootNull() throws Exception {
		server.startServer(null, 12473);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStartServer_ContextRootEmpty() throws Exception {
		server.startServer("", 12473);
	}

	@Test
	public void testStartServer_WarFile() throws Exception {

		server.startServer("test-web-app.war", 12473);
		assertTrue(isServerRunning());
	}

	@Test
	public void testStartServer_WebAppInProject_ContextRootHasLeadingSlash() throws Exception {

		server.startServer("/foo", 12473);
		assertTrue(isServerRunning());
	}

	@Test
	public void testStartServer_WebAppInProject_ContextRootHasNoLeadingSlash() throws Exception {

		server.startServer("/foo", 12473);
		assertTrue(isServerRunning());
	}

	private boolean isServerRunning() throws Exception {
		return AbstractWebServerTest.isServerRunning("http://localhost:12473/foo");
	}
}
