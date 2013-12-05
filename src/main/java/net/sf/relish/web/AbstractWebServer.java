package net.sf.relish.web;

import static net.sf.relish.RelishUtil.*;
import net.sf.relish.RelishException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

/**
 * Abstract class for web servers/services that run a Jetty server
 */
public abstract class AbstractWebServer {

	protected Server server;

	/**
	 * Starts the Jetty server on the specified port with the specified handler
	 */
	protected final void startServer(int serverPort, Handler handler) throws Exception {

		if (server != null) {
			throw new RelishException("You may not start the web server because it is already running.");
		}

		validateInRange("serverPort", serverPort, 1, 0xffff);
		validateNotNull("handler", handler);

		server = new Server(serverPort);
		server.setHandler(handler);
		server.start();
	}

	/**
	 * Stops the Jetty server if it is running
	 */
	public final void stopServer() throws Exception {

		if (server == null) {
			return;
		}

		server.stop();
		server.join();
		server = null;
	}

	/**
	 * @return True if the server is running
	 */
	public final boolean isRunning() {
		return server == null ? false : server.isRunning();
	}
}
