package com.test.salesportal.rest.search.all;

import java.util.List;

import com.test.salesportal.model.SortAttribute;

interface SortedSearchResultFactory {

	SortedSearchResult createSortedSearchResult(String searchResultId, List<SortAttribute> sortAttributes, int indexIntoFields);
	
}
