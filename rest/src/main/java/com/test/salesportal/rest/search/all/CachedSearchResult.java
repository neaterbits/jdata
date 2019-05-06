package com.test.salesportal.rest.search.all;

import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;

final class CachedSearchResult {
	private final String searchResultId;
	private final int totalItemMatchCount;
	private final SearchFacetsResult facets;

	CachedSearchResult(String searchResultId, int totalItemMatchCount, SearchFacetsResult facets) {
		this.searchResultId = searchResultId;
		this.totalItemMatchCount = totalItemMatchCount;
		this.facets = facets;
	}

	String getSearchResultId() {
		return searchResultId;
	}
	
	int getTotalItemMatchCount() {
		return totalItemMatchCount;
	}

	SearchFacetsResult getFacets() {
		return facets;
	}
}
