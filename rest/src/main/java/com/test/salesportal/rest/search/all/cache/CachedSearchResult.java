package com.test.salesportal.rest.search.all.cache;

import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;

public final class CachedSearchResult {
	private final String searchResultId;
	private final int totalItemMatchCount;
	private final SearchFacetsResult facets;

	CachedSearchResult(String searchResultId, int totalItemMatchCount, SearchFacetsResult facets) {
		this.searchResultId = searchResultId;
		this.totalItemMatchCount = totalItemMatchCount;
		this.facets = facets;
	}

	public String getSearchResultId() {
		return searchResultId;
	}
	
	public int getTotalItemMatchCount() {
		return totalItemMatchCount;
	}

	public SearchFacetsResult getFacets() {
		return facets;
	}
}
