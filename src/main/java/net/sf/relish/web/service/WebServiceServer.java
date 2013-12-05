package net.sf.relish.web.service;

import static net.sf.relish.RelishUtil.*;
import net.sf.relish.RelishException;
import net.sf.relish.web.AbstractWebServer;

/**
 * Configures and runs the jetty server for mock web services.
 */
final class WebServiceServer extends AbstractWebServer {

	private final WebServiceHandler webServiceHandler;

	private int serverPort;

	public WebServiceServer(WebServiceHandler webServiceHandler) {

		this.webServiceHandler = webServiceHandler;
	}

	/**
	 * Starts the Jetty server on the specified port if it is not already running
	 */
	public void startServer(int serverPort) throws Exception {

		if (server != null) {
			if (serverPort != this.serverPort) {
				throw new RelishException("You may not start the HTTP server on port %d because it is already running on port %d", serverPort, this.serverPort);
			}
			return;
		}

		this.serverPort = validateInRange("serverPort", serverPort, 1, 0xffff);

		super.startServer(serverPort, webServiceHandler);
	}
}
