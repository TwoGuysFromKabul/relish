package net.sf.relish.web.app;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import net.sf.relish.RelishException;
import net.sf.relish.web.AbstractWebServerTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebAppStepDefsTest {

	@Rule public final ExpectedException expectedException = ExpectedException.none();
	WebAppStepDefs steps = new WebAppStepDefs();

	@After
	public void after() throws Exception {
		steps.after();
	}

	@Test
	public void testAfter_WebAppNotRunning() throws Exception {

		steps.after();
	}

	@Test
	public void testAfter_WebAppRunning_InProject() throws Exception {

		steps.webAppIsRunningOnPort("test-web-app", 12473);
		assertTrue(isServerRunning());
		steps.after();
		assertFalse(isServerRunning());
	}

	@Test
	public void testAfter_WebAppRunning_FromWar() throws Exception {

		steps.webAppIsRunningOnPort("test-web-app.war", 12473);
		assertTrue(isServerRunning());
		steps.after();
		assertFalse(isServerRunning());
	}

	@Test
	public void testAfter_MultipleWebAppsRunning() throws Exception {

		steps.webAppIsRunningOnPort("test-web-app2", 12473);
		steps.webAppIsRunningOnPort("test-web-app.war", 12474);
		assertTrue(AbstractWebServerTest.isServerRunning("http://localhost:12473/test-web-app2"));
		assertTrue(AbstractWebServerTest.isServerRunning("http://localhost:12474/test-web-app"));

		steps.after();
		assertFalse(AbstractWebServerTest.isServerRunning("http://localhost:12473/test-web-app2"));
		assertFalse(AbstractWebServerTest.isServerRunning("http://localhost:12474/test-web-app"));
	}

	@Test
	public void testWebAppIsRunningOnPort_WebAppAlreadyRunning() throws Exception {

		expectedException.expect(RelishException.class);
		expectedException.expectMessage("You may not start web app test-web-app because it is already running");

		steps.webAppIsRunningOnPort("test-web-app", 12473);
		steps.webAppIsRunningOnPort("test-web-app", 12473);
	}

	@Test
	public void testWebAppIsRunningOnPort_InProject() throws Exception {

		steps.webAppIsRunningOnPort("test-web-app", 12473);
		assertTrue(isServerRunning());
	}

	@Test
	public void testWebAppIsRunningOnPort_FromWar() throws Exception {

		steps.webAppIsRunningOnPort("test-web-app.war", 12473);
		assertTrue(isServerRunning());
	}

	@Test
	public void testWebAppIsRunningOnPort_MultipleAppsRunning() throws Exception {

		assertFalse(AbstractWebServerTest.isServerRunning("http://localhost:12473/test-web-app2"));
		assertFalse(AbstractWebServerTest.isServerRunning("http://localhost:12474/test-web-app"));

		steps.webAppIsRunningOnPort("test-web-app2", 12473);
		assertTrue(AbstractWebServerTest.isServerRunning("http://localhost:12473/test-web-app2"));
		assertFalse(AbstractWebServerTest.isServerRunning("http://localhost:12474/test-web-app"));

		steps.webAppIsRunningOnPort("test-web-app.war", 12474);
		assertTrue(AbstractWebServerTest.isServerRunning("http://localhost:12473/test-web-app2"));
		assertTrue(AbstractWebServerTest.isServerRunning("http://localhost:12474/test-web-app"));
	}

	@Test
	public void testWebAppIsStopped_NotRunning() throws Exception {

		expectedException.expect(RelishException.class);
		expectedException.expectMessage("You cannot stop web app test-web-app because it is not running");

		steps.webAppIsStopped("test-web-app");
	}

	@Test
	public void testWebAppIsStopped_InProject() throws Exception {

		steps.webAppIsRunningOnPort("test-web-app", 12473);
		assertTrue(isServerRunning());
		steps.webAppIsStopped("test-web-app");
	}

	@Test
	public void testWebAppIsStopped_FromWar() throws Exception {

		steps.webAppIsRunningOnPort("test-web-app.war", 12473);
		assertTrue(isServerRunning());
		steps.webAppIsStopped("test-web-app.war");
	}

	private boolean isServerRunning() throws Exception {
		return AbstractWebServerTest.isServerRunning("http://localhost:12473/test-web-app");
	}
}
