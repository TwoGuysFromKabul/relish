package net.sf.relish.web.service;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;

import net.sf.relish.RelishException;
import net.sf.relish.web.AbstractWebServerTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebServiceServerTest {

	Map<String, WebServiceConfig> webServiceConfigByName = new HashMap<String, WebServiceConfig>();
	WebServiceHandler handler = new WebServiceHandler(webServiceConfigByName);
	WebServiceServer server = new WebServiceServer(handler);
	WebServiceConfig config = new WebServiceConfig();

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		config.enable("/foo");
		webServiceConfigByName.put("/foo", config);
	}

	@After
	public void after() throws Exception {
		if (server != null) {
			server.stopServer();
		}
	}

	@Test
	public void testStartServer() throws Exception {
		server.startServer(12473);
		assertTrue(isServerRunning());
		assertTrue(server.isRunning());
	}

	@Test
	public void testStartServer_ServerAlreadyRunning_OnSamePort() throws Exception {
		server.startServer(12473);
		server.startServer(12473);
	}

	@Test(expected = RelishException.class)
	public void testStartServer_ServerAlreadyRunning_OnDifferentPort() throws Exception {
		server.startServer(12473);
		server.startServer(12474);
	}

	private boolean isServerRunning() throws Exception {
		return AbstractWebServerTest.isServerRunning("http://localhost:12473/foo");
	}
}
