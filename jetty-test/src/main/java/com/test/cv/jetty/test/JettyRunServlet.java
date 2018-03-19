package com.test.cv.jetty.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.test.cv.common.IOUtil;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.model.Item;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.model.items.TypeInfo;
import com.test.cv.rest.ItemService;
import com.test.cv.rest.SearchCriteria;
import com.test.cv.rest.SearchResult;
import com.test.cv.rest.SearchService;

public class JettyRunServlet {

	public static void main(String [] args) throws Exception {
		
		final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

		//final ServletHandler contextHandler = new ServletHandler();
		
		
		final Server server = new Server(8080);
		
		server.setHandler(contextHandler);
		
		contextHandler.addServlet(SearchServlet.class, "/search/*");
		contextHandler.addServlet(ItemServlet.class, "/items/*");
		//contextHandler.addServletWithMapping(SearchServlet.class, "/search/*");
		
		try {
			server.start();
			server.join();
		}
		finally {
			server.destroy();
		}
	}

	public static class SearchServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		private static boolean isTest(HttpServletRequest req) {
			final String testParam = req.getParameter("test");
			
			boolean test = "true".equals(testParam);

			return test;
		}
		
		@Override
		protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {

			if (isTest(req)) {
				resp.setHeader("Access-Control-Allow-Origin", "*");
			}
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			// TODO Auto-generated method stub

			//super.doGet(req, resp);
			
			//throw new UnsupportedOperationException();
			
			final SearchService searchService = new SearchService();
			
			// Get parameters
			String freeText = null;
			
			String [] types = req.getParameterValues("types");
			
			final String pageNoString = req.getParameter("pageNo");
			final int pageNo = pageNoString == null ? 1 : Integer.parseInt(pageNoString);
			
			final String itemsPerPageString = req.getParameter("itemsPerPage");
			final int itemsPerPage = itemsPerPageString == null ? Integer.MAX_VALUE : Integer.parseInt(itemsPerPageString);			
			System.out.println("Types: " + Arrays.toString(types));
			
			final SearchCriteria searchCriteria;
			
			final ObjectMapper mapper = new ObjectMapper();
			
			mapper.setSerializationInclusion(Include.NON_NULL);
			
			if (req.getContentLength() > 0) {
				// Decode JSON
						
				final String json = new String(IOUtil.readAll(req.getInputStream()));
				
				System.out.println("Got json \"" + json + "\"");
				
				searchCriteria = mapper.readValue(json, SearchCriteria.class);
			}
			else {
				searchCriteria = null;
			}

			final boolean testdata = "true".equals(req.getParameter("testdata"));

			final SearchResult result = searchService.search(
					freeText,
					types,
					searchCriteria != null ? searchCriteria.getCriteria() : null,
					pageNo,
					itemsPerPage,
					testdata,
					req);

			if (isTest(req)) {
				resp.setHeader("Access-Control-Allow-Origin", "*");
			}

			mapper.writerWithDefaultPrettyPrinter().writeValue(resp.getOutputStream(), result);

			resp.setStatus(200);
		}
	}
	
	public static class ItemServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			System.out.println("## post to items servlet");
			
			final ItemService itemService = new ItemService();
			final String userId = req.getParameter("userId");

			if (req.getPathInfo() != null && req.getPathInfo().contains("image")) {
				// Posting image
				final String itemId = req.getPathInfo().split("/")[1];
				
				try {
					itemService.storeImage(userId, itemId, IOUtil.readAll(req.getInputStream()), req);
				} catch (ItemStorageException ex) {
					throw new ServletException("Failed to store image", ex);
				}
			}
			else {
				// Posting item
				final String type = req.getParameter("itemType");

				if (type == null) {
					throw new ServletException("No type information for item");
				}

				final TypeInfo typeInfo = ItemTypes.getTypeByName(type);
				
				if (typeInfo == null) {
					throw new ServletException("Unknown type " + type);
				}

				final ObjectMapper mapper = new ObjectMapper();
				
				mapper.setDateFormat(new StdDateFormat());
				
				final byte [] data = IOUtil.readAll(req.getInputStream());
				
				System.out.println("Received data:\n" + new String(data));
				
				final Item item = mapper.readValue(new ByteArrayInputStream(data), typeInfo.getType());
				
				try {
					itemService.storeItem(userId, item, req);
				} catch (ItemStorageException ex) {
					throw new ServletException("Failed to store item", ex);
				}
			}
		}
	}
}
