package com.test.salesportal.jetty.test;

import java.io.File;
import java.io.FileInputStream;
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
import com.test.salesportal.common.IOUtil;
import com.test.salesportal.common.images.ThumbAndImageUrls;
import com.test.salesportal.dao.ItemStorageException;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.rest.BaseServiceLogic;
import com.test.salesportal.rest.items.ItemService;
import com.test.salesportal.rest.items.model.ServiceItem;
import com.test.salesportal.rest.search.SearchResult;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.paged.PagedSearchService;
import com.test.salesportal.rest.smslogin.LoginService;
import com.test.salesportal.rest.smslogin.LoginService.CheckCodeResponse;
import com.test.salesportal.rest.smslogin.LoginService.LoginResponse;

public class JettyRunServlet {

	private static boolean isTest() {
		return BaseServiceLogic.isTest();
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

		final String pagesDir;
		
		if (args.length >= 2) {
			pagesDir = args[1];
			
			final File dir = new File(pagesDir);
			
			if (!dir.exists() || !dir.isDirectory()) {
				throw new IllegalArgumentException("No directory at " + pagesDir);
			}
		}
		else {
			pagesDir = null;
		}

		contextHandler.setInitParameter("localFileDir", localFileDir);
		
		if (pagesDir != null) {
			contextHandler.setInitParameter("pagesDir", pagesDir);
		}
		
		final Server server = new Server(8080);
		
		server.setHandler(contextHandler);

		contextHandler.addServlet(SearchServlet.class, "/searchpaged/*");
		contextHandler.addServlet(ItemServlet.class, "/items/*");
		contextHandler.addServlet(LoginServlet.class, "/login/*");
		//contextHandler.addServletWithMapping(SearchServlet.class, "/search/*");

		contextHandler.addServlet(PagesServlet.class, "/*");
		
		try {
			server.start();
			server.join();
		}
		finally {
			server.destroy();
		}
	}
	
	
	public static class PagesServlet extends BaseServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			final String pathInfo = req.getPathInfo();
			
			String trimmed = pathInfo.trim();
			
			if (trimmed.isEmpty() || "/".equals(trimmed)) {
				trimmed = "facets.jsp";
			}
			else if (trimmed.startsWith("/")) {
				trimmed = trimmed.substring(1);
			}
			
			final String path = getPagesDir() + '/' + trimmed;

			if (path.endsWith("/facets.jsp")) {
				final String address = req.getServerName() + ':' + req.getServerPort();
				
				// replace local address
				try (FileInputStream inputStream = new FileInputStream(path)) {
					final byte [] data = IOUtil.readAll(inputStream);
					
					final String string = new String(data)
							.replace("localhost:8080", address);
					
					resp.getOutputStream().write(string.getBytes());
				
					resp.setStatus(200);
				}
			}
			else {
			
				try (FileInputStream inputStream = new FileInputStream(path)) {
					IOUtil.copyStreams(inputStream, resp.getOutputStream());
					
					resp.setStatus(200);
				}
			}
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
			
			final PagedSearchService searchService = new PagedSearchService(getLocalFileDir());
			
			// Get parameters
			String freeText = req.getParameter("freeText");
			
			String [] types = req.getParameterValues("itemType");
			
			final String pageNoString = req.getParameter("pageNo");
			final int pageNo = pageNoString == null ? 1 : Integer.parseInt(pageNoString);
			
			final String itemsPerPageString = req.getParameter("itemsPerPage");
			final int itemsPerPage = itemsPerPageString == null ? Integer.MAX_VALUE : Integer.parseInt(itemsPerPageString);			
			System.out.println("Types: " + Arrays.toString(types));

			final String [] sortOrder = req.getParameterValues("sortOrder");
			final String [] fields = req.getParameterValues("field");
			
			final SearchCriterium [] searchCriteria;
			
			final ObjectMapper mapper = JSONUtil.createMapper();
			
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
					fields,
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
				
				final PagedSearchService searchService = new PagedSearchService(getLocalFileDir());

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

			if (req.getPathInfo() != null && req.getPathInfo().contains("thumbAndImageUrls")) {
				final ThumbAndImageUrls thumbAndImageUrls = JSONUtil.decodeJson(IOUtil.readAll(req.getInputStream()), ThumbAndImageUrls.class);

				final String itemId = req.getPathInfo().split("/")[1];
				
				final String itemType = req.getParameter("itemType");

				try {
					itemService.storeThumbAndImageUrls(userId, itemId, itemType, thumbAndImageUrls, req);
				} catch (ItemStorageException ex) {
					throw new ServletException("Failed to store image URLs", ex);
				}
			}
			else if (req.getPathInfo() != null && req.getPathInfo().contains("imageThumbAndUrl")) {
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
				
				final byte [] data = IOUtil.readAll(req.getInputStream());
				
				System.out.println("Received data:\n" + new String(data));
				
				final Item item = JSONUtil.decodeJson(data, typeInfo.getType());
				
				try {
					final String itemId = itemService.storeItem(userId, item, req);
					
					resp.getOutputStream().write(itemId.getBytes());
				} catch (ItemStorageException ex) {
					throw new ServletException("Failed to store item", ex);
				}
			}
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			final ItemService itemService = new ItemService(getLocalFileDir());

			if (isTest()) {
				resp.setHeader("Access-Control-Allow-Origin", "*");
			}
			
			String [] path;

			if (req.getPathInfo() != null && req.getPathInfo().contains("thumbs")) {
				
				// items/{itemId}/photos/{photoNo}
				path = req.getPathInfo().split("/");
				final String itemId = path[1];
				final int thumbNo = Integer.parseInt(path[3]);
				
				try {
					final byte [] data = itemService.getThumb(itemId, thumbNo, req);

					resp.getOutputStream().write(data);
					resp.getOutputStream().close();
				} catch (ItemStorageException ex) {
					throw new ServletException("Failed to get photo", ex);
				}
			}
			else if (req.getPathInfo() != null && req.getPathInfo().contains("photoCount")) {
				final String itemId = req.getPathInfo().split("/")[1];

				try {
					final int count = itemService.getPhotoCount(itemId, req);
					
					System.out.println("## write photo count " + count);
					
					resp.getOutputStream().write(String.valueOf(count).getBytes());
					resp.getOutputStream().close();
				} catch (ItemStorageException ex) {
					throw new ServletException("Failed to get photo", ex);
				}
			}
			else if (req.getPathInfo() != null && req.getPathInfo().contains("photos")) {
				
				// items/{itemId}/photos/{photoNo}
				path = req.getPathInfo().split("/");
				final String itemId = path[1];
				final int photoNo = Integer.parseInt(path[3]);
				
				try {
					final byte [] data = itemService.getPhoto(itemId, photoNo, req);

					resp.getOutputStream().write(data);
					resp.getOutputStream().close();
				} catch (ItemStorageException ex) {
					throw new ServletException("Failed to get photo", ex);
				}
			}
			else if ((path = req.getPathInfo().split("/")).length == 2) {
				// items/{itemId}
				final String itemId = path[1];

				try {
					final ServiceItem item = itemService.getItem(itemId, req);
					
					if (item == null) {
						resp.sendError(404);
					}
					else {
						JSONUtil.encodeJson(item, resp.getOutputStream());
						resp.getOutputStream().close();
					}
				} catch (ItemStorageException ex) {
					throw new ServletException("Failed to get item", ex);
				}
			}
			else {
				super.doGet(req, resp);
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

				JSONUtil.encodeJson(response, resp.getOutputStream());
			}
			else if (req.getPathInfo() != null && req.getPathInfo().contains("/checkcode")) {
				// This is register or login scenario, check if user exist
				final LoginService loginService = new LoginService();
				
				final CheckCodeResponse response =  loginService.checkCode(req.getParameter("phoneNo"), req.getParameter("code"));

				JSONUtil.encodeJson(response, resp.getOutputStream());
			}
			else {
				super.doPost(req, resp);
			}
		}
	}
}
