package net.sf.relish.web.app;

import java.util.HashMap;
import java.util.Map;

import net.sf.relish.RelishException;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;

public final class WebAppStepDefs {

	private final Map<String, WebAppServer> serverByContextRootOrWarFile = new HashMap<String, WebAppServer>();

	/**
	 * Runs after each relish scenario
	 */
	@After
	public void after() throws Exception {

		for (WebAppServer server : serverByContextRootOrWarFile.values()) {
			server.stopServer();
		}
	}

	/**
	 * Starts a web app either from the local project or from a war file
	 * 
	 * @param contextRootOrWarFile
	 *            The web app's context root or WAR file. If this point to a file it will be considered a WAR file and the context root will be the file's name
	 *            with the path and ".war" trimmed off. If this is not a file then it will be the context root of the web app, the classes will be assumed to
	 *            already be on the classpath, and the document root (where WEB-INF is located) will be "./src/main/webapp".
	 * @param serverPort
	 *            The port to run the server on
	 */
	@Given("^web app \"(\\S.+\\S)\" is running on port (\\d{2,5})$")
	public void webAppIsRunningOnPort(String contextRootOrWarFile, int serverPort) throws Exception {

		WebAppServer server = serverByContextRootOrWarFile.get(contextRootOrWarFile);
		if (server != null) {
			throw new RelishException("You may not start web app %s because it is already running", contextRootOrWarFile);
		}

		server = new WebAppServer();
		serverByContextRootOrWarFile.put(contextRootOrWarFile, server);

		server.startServer(contextRootOrWarFile, serverPort);
	}

	/**
	 * Stops a web app previously started with "Web app "..." is running on port ##".
	 * 
	 * @param contextRootOrWarFile
	 *            The context root or WAR file specified when the web app was started.
	 */
	@Given("^web app \"(\\S.+\\S)\" is stopped$")
	public void webAppIsStopped(String contextRootOrWarFile) throws Exception {

		WebAppServer server = serverByContextRootOrWarFile.remove(contextRootOrWarFile);
		if (server == null) {
			throw new RelishException("You cannot stop web app %s because it is not running", contextRootOrWarFile);
		}
		server.stopServer();
	}
}
