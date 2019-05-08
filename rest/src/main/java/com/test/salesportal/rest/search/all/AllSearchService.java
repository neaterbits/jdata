package com.test.salesportal.rest.search.all;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.test.salesportal.dao.IOperationsDAO;
import com.test.salesportal.rest.search.BaseSearchService;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.paged.ItemSearchResult;

@Path("/searchall")
public final class AllSearchService
	extends BaseSearchService<AllSearchItemResult, ItemSearchResult<AllSearchItemResult>> {

	
	private final AllSearchLogic searchLogic;
	
	public AllSearchService(String localFileDir, IOperationsDAO operationsDAO) {
		super(localFileDir);
		
		this.searchLogic = new AllSearchLogic(operationsDAO);
	}
	

	@GET
	@Path("search")
	public AllSearchResult search(
			String [] types,
			String freeText,
			SearchCriterium [] criteria,
			String [] sortOrder,
			String [] fields,
			HttpServletRequest request) {

		return searchLogic.search(types, freeText, criteria, sortOrder, fields, getSearchDAO(request));
	}
}
