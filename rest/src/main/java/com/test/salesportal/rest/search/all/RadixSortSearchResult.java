package com.test.salesportal.rest.search.all;

import java.util.List;

import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.model.SortOrder;

/**
 * Radix sort for faster insertion
 * 
 */
final class RadixSortSearchResult extends SortedSearchResult {

	RadixSortSearchResult(String searchResultId, List<SortAttribute> sortAttributes, int indexIntoFields) {
		super(searchResultId, sortAttributes, indexIntoFields);
	}

	@Override
	void insertSearchResults(AllSearchItemResult[] searchResults) {
		// TODO Auto-generated method stub
		
	}

	@Override
	AllSearchItemResult[] getSearchResults(int index, int count, SortOrder sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	int getMatchCount() {
		// TODO Auto-generated method stub
		return 0;
	}
}
