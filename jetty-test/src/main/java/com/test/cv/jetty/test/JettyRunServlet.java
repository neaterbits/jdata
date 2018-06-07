package com.test.cv.jetty.test;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.test.cv.common.IOUtil;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.model.Item;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.model.items.TypeInfo;
import com.test.cv.rest.BaseService;
import com.test.cv.rest.ItemService;
import com.test.cv.rest.LoginService;
import com.test.cv.rest.LoginService.CheckCodeResponse;
import com.test.cv.rest.LoginService.LoginResponse;
import com.test.cv.rest.SearchCriterium;
import com.test.cv.rest.SearchResult;
import com.test.cv.rest.SearchService;

public class JettyRunServlet {

	private static boolean isTest() {
		return BaseService.isTest();
	}
	

	public static void main(String [] args) throws Exception {
		
		final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

		//final ServletHandler contextHandler = new ServletHandler();
		
		final String localFileDir;
		if (args.length >= 1) {
			localFileDir = args[0];
			
			final File dir = new File(localFileDir);
			
			if (!dir.exists() || !dir.isDirectory()) {
				throw new IllegalArgumentException("No directory at " + localFileDir);
			}
		}
		else {
			throw new IllegalArgumentException("Must pass local file dir");
		}
		
		final Server server = new Server(8080);
		
		server.setHandler(contextHandler);
		
		contextHandler.setInitParameter("localFileDir", localFileDir);
		
		contextHandler.addServlet(SearchServlet.class, "/search/*");
		contextHandler.addServlet(ItemServlet.class, "/items/*");
		contextHandler.addServlet(LoginServlet.class, "/login/*");
		//contextHandler.addServletWithMapping(SearchServlet.class, "/search/*");
		
		try {
			server.start();
			server.join();
		}
		finally {
			server.destroy();
		}
	}

	public static class SearchServlet extends BaseServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {

			if (isTest()) {
				resp.setHeader("Access-Control-Allow-Origin", "*");
				resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
			}
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			// TODO Auto-generated method stub

			//super.doGet(req, resp);
			
			//throw new UnsupportedOperationException();
			
			final SearchService searchService = new SearchService(getLocalFileDir());
			
			// Get parameters
			String freeText = req.getParameter("freeText");
			
			String [] types = req.getParameterValues("itemType");
			
			final String pageNoString = req.getParameter("pageNo");
			final int pageNo = pageNoString == null ? 1 : Integer.parseInt(pageNoString);
			
			final String itemsPerPageString = req.getParameter("itemsPerPage");
			final int itemsPerPage = itemsPerPageString == null ? Integer.MAX_VALUE : Integer.parseInt(itemsPerPageString);			
			System.out.println("Types: " + Arrays.toString(types));

			final String [] sortOrder = req.getParameterValues("sortOrder");
			
			final SearchCriterium [] searchCriteria;
			
			final ObjectMapper mapper = new ObjectMapper();
			
			mapper.setSerializationInclusion(Include.NON_NULL);
			
			if (req.getContentLength() > 0) {
				// Decode JSON
						
				final String json = new String(IOUtil.readAll(req.getInputStream()));
				
				System.out.println("Got json \"" + json + "\"");
				
				searchCriteria = mapper.readValue(json, SearchCriterium[].class);
			}
			else {
				searchCriteria = null;
			}

			final boolean testdata = "true".equals(req.getParameter("testdata"));

			final SearchResult result = searchService.search(
					types,
					freeText,
					searchCriteria,
					sortOrder,
					pageNo,
					itemsPerPage,
					testdata,
					req);

			if (isTest()) {
				resp.setHeader("Access-Control-Allow-Origin", "*");
			}

			mapper.writerWithDefaultPrettyPrinter().writeValue(resp.getOutputStream(), result);

			resp.setStatus(200);
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			
			if (isTest()) {
				resp.setHeader("Access-Control-Allow-Origin", "*");
			}

			if (req.getPathInfo() != null && req.getPathInfo().contains("thumbnails")) {

				// Retrieve thumbnails as a stream
				final String [] itemIds = req.getParameterValues("itemId");
				
				if (itemIds == null) {
					throw new IllegalArgumentException("No itemIds");
				}
				
				final SearchService searchService = new SearchService(getLocalFileDir());

				final byte [] data = searchService.getThumbnails(itemIds, req);
				
				resp.getOutputStream().write(data);
				
				resp.getOutputStream().close();
			}
		}
	}
	
	public static class ItemServlet extends BaseServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			System.out.println("## post to items servlet");
			
			final ItemService itemService = new ItemService(getLocalFileDir());
			final String userId = req.getParameter("userId");

			if (req.getPathInfo() != null && req.getPathInfo().contains("imageThumbAndUrl")) {
				final int index = Integer.parseInt(req.getParameter("index"));
				final int thumbWidth = Integer.parseInt(req.getParameter("thumbWidth"));
				final int thumbHeight = Integer.parseInt(req.getParameter("thumbHeight"));
				
				final String imageUrl = req.getParameter("imageUrl");
				
				// Posting image
				final String itemId = req.getPathInfo().split("/")[1];
				
				final String itemType = req.getParameter("itemType");
				
				try {
					itemService.storeThumbAndImageUrl(userId, itemId, itemType, index, thumbWidth, thumbHeight, imageUrl, IOUtil.readAll(req.getInputStream()), req);
				} catch (ItemStorageException ex) {
					throw new ServletException("Failed to store image", ex);
				}
			}
			else if (req.getPathInfo() != null && req.getPathInfo().contains("image")) {
				final int index = Integer.parseInt(req.getParameter("index"));
				// Posting image
				final String itemId = req.getPathInfo().split("/")[1];
				
				final String itemType = req.getParameter("itemType");
				
				try {
					itemService.storeImage(userId, itemId, itemType, index, IOUtil.readAll(req.getInputStream()), req);
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
					final String itemId = itemService.storeItem(userId, item, req);
					
					resp.getOutputStream().write(itemId.getBytes());
				} catch (ItemStorageException ex) {
					throw new ServletException("Failed to store item", ex);
				}
			}
		}
	}
	
	public static class LoginServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			if (isTest()) {
				resp.setHeader("Access-Control-Allow-Origin", "*");
			}

			if (req.getPathInfo() != null && req.getPathInfo().contains("/checkphoneno")) {
				// This is register or login scenario, check if user exist
				final LoginService loginService = new LoginService();
				
				final LoginResponse response =  loginService.checkPhoneNo(req.getParameter("phoneNo"));

				final ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(resp.getOutputStream(), response);
				
			}
			else if (req.getPathInfo() != null && req.getPathInfo().contains("/checkcode")) {
				// This is register or login scenario, check if user exist
				final LoginService loginService = new LoginService();
				
				final CheckCodeResponse response =  loginService.checkCode(req.getParameter("phoneNo"), req.getParameter("code"));

				final ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(resp.getOutputStream(), response);
			}
			else {
				super.doPost(req, resp);
			}
		}
	}
}
