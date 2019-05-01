package com.test.salesportal.jetty.test;

import javax.servlet.http.HttpServlet;

public abstract class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	final String getLocalFileDir() {
		return getServletContext().getInitParameter("localFileDir");
	}
	
}
