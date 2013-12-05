package net.sf.relish.web.app;

import static net.sf.relish.RelishUtil.*;

import java.io.File;

import net.sf.relish.RelishException;
import net.sf.relish.web.AbstractWebServer;

import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Configures and runs the jetty server for the web application under test.
 */
final class WebAppServer extends AbstractWebServer {

	/**
	 * Starts the Jetty server on the specified port
	 * 
	 * @param contextRootOrWarFile
	 *            This may be either the context root of a web app in the local project or a war file path. If it is a context root then the web application's
	 *            config is assumed to be in ./src/main/webapp.
	 * @param serverPort
	 *            The port to run the server on
	 */
	public void startServer(String contextRootOrWarFile, int serverPort) throws Exception {

		if (server != null) {
			throw new RelishException("You may not start web app %s because it is already running.", contextRootOrWarFile);
		}

		validateNotEmpty("contextRootOrWarFile", contextRootOrWarFile);

		// This property is just for the JmxDiscoveryClient. It will not enable the JMX platform server.
		System.setProperty("com.sun.management.jmxremote.port", "19999");

		WebAppContext webapp = new WebAppContext();

		String contextPath;
		File warFile = new File(contextRootOrWarFile);
		if (warFile.isFile()) {
			String name = warFile.getName();
			int extStart = name.lastIndexOf('.');
			contextPath = extStart > 0 ? name.substring(0, extStart) : name;
			webapp.setWar(warFile.getAbsolutePath());
		} else {
			String resourceBase;
			if (new File("./src/main/webapp").isDirectory()) {
				resourceBase = "./src/main/webapp";
			} else if (new File("./src/test/webapp").isDirectory()) {
				resourceBase = "./src/test/webapp";
			} else {
				throw new RelishException("Webapp resource base directory not found: ./src/main/webapp");
			}
			contextPath = contextRootOrWarFile;
			webapp.setResourceBase(resourceBase);
			webapp.setParentLoaderPriority(true);
		}

		if (!contextPath.startsWith("/")) {
			contextPath = "/" + contextPath;
		}
		webapp.setContextPath(contextPath);

		super.startServer(serverPort, webapp);
		webapp.start();
	}
}
