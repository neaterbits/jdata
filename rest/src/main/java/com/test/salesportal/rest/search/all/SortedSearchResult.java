package com.test.salesportal.rest.search.all;

import java.util.List;

import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.model.SortOrder;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;

abstract class SortedSearchResult {

	static final AllSearchItemResult [] EMPTY_ARRAY = new AllSearchItemResult[0];

	private final String searchResultId;
	
	private final List<SortAttribute> sortAttributes;
	private final int indexIntoFields;
	
	private SearchFacetsResult facetsResult;
	
	abstract void insertSearchResults(AllSearchItemResult [] searchResults);
	
	abstract AllSearchItemResult [] getSearchResults(int index, int count, SortOrder sortOrder);
	
	abstract int getMatchCount();

	SortedSearchResult(String searchResultId, List<SortAttribute> sortAttributes, int indexIntoFields) {

		if (searchResultId == null) {
			throw new IllegalArgumentException("searchResultId == null");
		}
		
		if (sortAttributes == null) {
			throw new IllegalArgumentException("sortAttributes == null");
		}
	
		if (sortAttributes.isEmpty()) {
			throw new IllegalArgumentException("No sort attributes");
		}

		this.searchResultId = searchResultId;
		this.sortAttributes = sortAttributes;
		this.indexIntoFields = indexIntoFields;
	}
	
	void initFacetsResult(SearchFacetsResult facetsResult) {
		
		if (facetsResult == null) {
			throw new IllegalArgumentException("facetsResult");
		}
		
		if (this.facetsResult != null) {
			throw new IllegalStateException("Overwriting initial update from index");
		}
		
		this.facetsResult = facetsResult;
	}
	
	SearchFacetsResult getFacetsResult() {
		return facetsResult;
	}

	final String getSearchResultId() {
		return searchResultId;
	}

	final List<SortAttribute> getSortAttributes() {
		return sortAttributes;
	}

	final int getIndexIntoFields() {
		return indexIntoFields;
	}
}
