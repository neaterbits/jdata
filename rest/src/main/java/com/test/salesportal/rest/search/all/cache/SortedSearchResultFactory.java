package com.test.salesportal.rest.search.all.cache;

import java.util.List;

import com.test.salesportal.model.items.SortAttribute;

public interface SortedSearchResultFactory {

	SortedSearchResult createSortedSearchResult(String searchResultId, List<SortAttribute> sortAttributes);
	
}
