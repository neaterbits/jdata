package com.test.cv.jetty.test;

import java.io.File;
import java.lang.management.ManagementFactory;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

// See http://www.eclipse.org/jetty/documentation/current/embedded-examples.html
public class JettyRunDeployment {

	public static void main(String [] args) throws Exception {
		final Server server = new Server(7200);
		
		final MBeanContainer mBeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		
		server.addBean(mBeanContainer);
		
		final WebAppContext webapp = new WebAppContext();
		
		webapp.setContextPath("/");
		
		final File warFile = new File("../rest/target/com.test.cv/rest/target/rest");
		
		final String absolutePath = warFile.getAbsolutePath();
		
		System.out.println("## starting from " + absolutePath);
		webapp.setWar(absolutePath);

		server.setHandler(webapp);
		
		server.start();
		
		//server.dumpStdErr();
		
		server.join();
	}
}
