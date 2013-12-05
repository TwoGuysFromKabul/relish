package net.sf.relish;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is built into the test-web-app.war that is included in the project. It is used to test deploying a web app from a war into Jetty.
 */
public class TestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public TestServlet() {
		System.out.println("Constructing " + getClass().getName());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		System.out.println("Getting " + req.getRequestURL());
		resp.setStatus(200);
	}
}
