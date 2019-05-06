package com.test.salesportal.rest.search.all.cache;

import java.util.List;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.model.SortOrder;
import com.test.salesportal.rest.search.all.AllSearchItemResult;

/**
 * Radix sort for faster insertion
 * 
 */
final class RadixSortSearchResult extends SortedSearchResult {

	RadixSortSearchResult(String searchResultId, List<SortAttribute> sortAttributes) {
		super(searchResultId, sortAttributes);
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

	@Override
	void applyItemToCachedItems(SearchKey searchKey, Item item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void deleteItemFromCachedItems(String itemId) {
		// TODO Auto-generated method stub
		
	}
}
