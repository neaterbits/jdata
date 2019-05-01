package com.test.salesportal.jetty.test;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.test.salesportal.rest.SearchService;

public class JettyRunEmbedded {

	// https://stackoverflow.com/questions/38345748/jax-rs-with-embedded-jetty-service-home-url
	public static void main(String [] args) throws Exception {
		final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		
		contextHandler.setContextPath("/");
		
		final Server server = new Server(8080);
		
		server.setHandler(contextHandler);
		
		final ServletHolder servlet = contextHandler.addServlet(ServletContainer.class, "/*");
		
		// servlet.setInitParameter("jersey.config.server.provider.classnames", SearchService.class.getName());
		servlet.setInitParameter("jersey.config.server.provider.classes", SearchService.class.getName());
		
		try {
			server.start();
			server.join();
		}
		finally {
			server.destroy();
		}
	}
}
