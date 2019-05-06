package com.test.salesportal.rest.search;

import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;
import com.test.salesportal.rest.search.model.sorting.SearchSortOrderAlternative;

/**
 * Search result for one page of data
 */
public class SearchResult {

	private int totalItemMatchCount; // total number of matches, always pass along for convenience
	
	private SearchSortOrderAlternative [] sortOrders;
	
	private SearchFacetsResult facets;
	
	// Facet results, eg all attributes and the count for each
	// Some attributes are common for multiple datatypes, other are specific

	public SearchResult() {
		
		
	}
	
	public SearchResult(int totalItemMatchCount, SearchSortOrderAlternative[] sortOrders, SearchFacetsResult facets) {
		this.totalItemMatchCount = totalItemMatchCount;
		this.sortOrders = sortOrders;
		this.facets = facets;
	}

	public int getTotalItemMatchCount() {
		return totalItemMatchCount;
	}

	public void setTotalItemMatchCount(int totalItemMatchCount) {
		this.totalItemMatchCount = totalItemMatchCount;
	}

	public SearchSortOrderAlternative[] getSortOrders() {
		return sortOrders;
	}

	public void setSortOrders(SearchSortOrderAlternative[] sortOrders) {
		this.sortOrders = sortOrders;
	}

	public SearchFacetsResult getFacets() {
		return facets;
	}

	public void setFacets(SearchFacetsResult facets) {
		this.facets = facets;
	}
}
