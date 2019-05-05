package com.test.salesportal.rest.search.returnitems;

import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;
import com.test.salesportal.rest.search.model.sorting.SearchSortOrderAlternative;

/**
 * Search result for one page of data
 */
public class SearchResult {

	private int totalItemMatchCount; // total number of matches, always pass along for convenience
	private int pageFirstItem; // index of item into total
	private int pageItemCount; // number of items returned, same as item array length
	
	
	private SearchSortOrderAlternative [] sortOrders;
	
	private SearchFacetsResult facets;
	
	private SearchItemResult [] items;
	
	// Facet results, eg all attributes and the count for each
	// Some attributes are common for multiple datatypes, other are specific

	public int getTotalItemMatchCount() {
		return totalItemMatchCount;
	}

	public void setTotalItemMatchCount(int totalItemMatchCount) {
		this.totalItemMatchCount = totalItemMatchCount;
	}

	public int getPageFirstItem() {
		return pageFirstItem;
	}

	public void setPageFirstItem(int pageFirstItem) {
		this.pageFirstItem = pageFirstItem;
	}

	public int getPageItemCount() {
		return pageItemCount;
	}

	public void setPageItemCount(int pageItemCount) {
		this.pageItemCount = pageItemCount;
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

	public SearchItemResult [] getItems() {
		return items;
	}

	public void setItems(SearchItemResult[] items) {
		this.items = items;
	}
}
