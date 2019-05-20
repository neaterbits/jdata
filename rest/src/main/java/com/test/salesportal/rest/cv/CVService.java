package com.test.salesportal.rest.cv;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

import com.test.salesportal.dao.CVStorageException;
import com.test.salesportal.dao.ICVDAO;
import com.test.salesportal.dao.xml.XMLCVDAO;
import com.test.salesportal.model.cv.CV;
import com.test.salesportal.model.cv.CVItem;
import com.test.salesportal.model.cv.Personalia;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.rest.BaseService;

@Path("/cv")
public class CVService extends BaseService {

	private final ItemTypes itemTypes;
	
	public CVService(String localFileDir, ItemTypes itemTypes) {
		super(localFileDir);
		
		if (itemTypes == null) {
			throw new IllegalArgumentException("itemTypes == null");
		}
		
		this.itemTypes = itemTypes;
	}

	private ICVDAO getDAO(HttpServletRequest request) {
		
		final ICVDAO ret;
		
		final Storage storage = getStorageType(request);
		
		switch (storage) {
		case LOCAL_FILE_LUCENE:
			ret = new XMLCVDAO(getLocalXMLStorage(), null, itemTypes);
			break;
			
		default:
			throw new UnsupportedOperationException("TODO - unsupported storage " + storage);
		}
		
		return ret;
	}

	// return the complete CV
	@GET
	@Path("/{userId}")
	@Produces("application/json")
	public CV getCV(@PathParam("userId") String userId, @Context HttpServletRequest request) {
		
		try {
			return getDAO(request).findCV(userId, getLanguages(request));
		} catch (CVStorageException e) {
			throw new WebApplicationException(500);
		} 
	}

	// get personalia
	@GET
	@Path("/{userId}/personalia")
	@Produces("text/plain")
	public Personalia getPersonalia(@PathParam("userId") String userId, @Context HttpServletRequest request) {
		return getCV(userId, request).getPersonalia();
	}
	
	// get all CV items to be displayed
	@GET
	@Path("/{userId}/items")
	@Produces("text/plain")
	public CVItem [] getItems(@PathParam("userId") String userId, @Context HttpServletRequest request) {
		final CV cv = getCV(userId, request);
		
		return cv.getItems() == null
				? new CVItem[0]
				: cv.getItems().toArray(new CVItem[cv.getItems().size()]);
	}
	
	// get details for one particular item
	@GET
	@Path("/{userId}/items/{itemNo}")
	@Produces("text/plain")
	public CVItem getItem(@PathParam("userId") String userId, @PathParam("itemNo") int itemNo, @Context HttpServletRequest request) {
		final CV cv = getCV(userId, request);
		
		return cv.getItems().get(itemNo);
	}
}
