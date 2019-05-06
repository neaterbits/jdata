package com.test.salesportal.rest.search.all;

import com.test.salesportal.rest.search.SearchResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;
import com.test.salesportal.rest.search.model.sorting.SearchSortOrderAlternative;

public class AllSearchResult extends SearchResult {

	private String searchResultId;
	
	public AllSearchResult() {

	}

	public AllSearchResult(
			int totalItemMatchCount,
			SearchSortOrderAlternative[] sortOrders,
			SearchFacetsResult facets,
			String searchResultId) {
		
		super(totalItemMatchCount, sortOrders, facets);
		
		this.searchResultId = searchResultId;
	}

	public String getSearchResultId() {
		return searchResultId;
	}

	public void setSearchResultId(String searchResultId) {
		this.searchResultId = searchResultId;
	}
}
