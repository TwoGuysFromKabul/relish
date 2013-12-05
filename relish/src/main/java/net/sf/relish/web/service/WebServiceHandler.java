package net.sf.relish.web.service;

import static net.sf.relish.RelishUtil.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.relish.NameValuePair;
import net.sf.relish.web.HttpResponseData;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Mock web services
 */
final class WebServiceHandler extends AbstractHandler {

	private final Map<String, WebServiceConfig> webServiceConfigByName;

	/**
	 * @param webServiceConfigByName
	 *            Map of {@link WebServiceConfig} objects keyed by their name. Used to configure the handling of requests. When a request is received these are
	 *            searched in their iteration order for one where the target of the request starts with the target specified in the {@link WebServiceConfig}.
	 *            The first config found is used. This map must have been created with {@link Collections#synchronizedMap(Map)}.
	 */
	public WebServiceHandler(Map<String, WebServiceConfig> webServiceConfigByName) {

		this.webServiceConfigByName = validateNotNull("webServiceConfigByName", webServiceConfigByName);
	}

	/**
	 * @see org.eclipse.jetty.server.Handler#handle(java.lang.String, org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		WebServiceConfig webServiceConfig = null;

		synchronized (webServiceConfigByName) {
			for (WebServiceConfig config : webServiceConfigByName.values()) {
				if (target.startsWith(config.getTarget())) {
					webServiceConfig = config;
					break;
				}
			}
		}

		if (webServiceConfig == null) {
			response.setStatus(HttpStatus.NOT_FOUND_404);
			return;
		}

		baseRequest.setHandled(true);

		HttpResponseData responseData = webServiceConfig.handleRequest(request);

		response.setStatus(responseData.getStatusCode());

		for (NameValuePair header : responseData.getHeaders()) {
			response.setHeader(header.getName(), header.getValue());
		}

		if (responseData.getBody() != null) {
			response.getOutputStream().write(responseData.getBody());
		}
	}
}
