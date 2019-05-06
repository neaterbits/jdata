package com.test.salesportal.rest.search.all;

import java.util.HashMap;
import java.util.Map;

import com.test.salesportal.common.UUIDGenerator;
import com.test.salesportal.model.SortOrder;
import com.test.salesportal.rest.search.SearchItemResult;

/**
 * Caches in this VM results by criteria, sorted in a particular order.
 * 
 * The cache is refreshed from the update log, depending on the revision number of the
 * whole sales portal model.
 * 
 */

final class SearchResultCache {

	private final SortedSearchResultFactory sortedSearchResultFactory;
	
	private final Map<SearchKey, SortedSearchResult> sortedResultByCriteria; 
	private final Map<String, SortedSearchResult> sortedResultBySearchResultId;
	
	public SearchResultCache(SortedSearchResultFactory sortedSearchResultFactory) {

		if (sortedSearchResultFactory == null) {
			throw new IllegalArgumentException("sortedSearchResultFactory == null");
		}
		
		this.sortedSearchResultFactory = sortedSearchResultFactory;
		
		this.sortedResultByCriteria = new HashMap<>();
		this.sortedResultBySearchResultId = new HashMap<>();
	}
	
	String cacheSearchResult(SearchKey searchKey, SearchItemResult [] searchResult, int indexOfFirstSortField) {
		
		if (searchKey == null) {
			throw new IllegalArgumentException("searchKey == null");
		}
		
		final String searchResultId = UUIDGenerator.generateUUID();
		
		final SortedSearchResult sortedSearchResult = sortedSearchResultFactory.createSortedSearchResult(
				searchResultId,
				searchKey.getSortAttributes(),
				indexOfFirstSortField);
		
		synchronized (this) {
			sortedResultByCriteria.put(searchKey, sortedSearchResult);
			sortedResultBySearchResultId.put(searchResultId, sortedSearchResult);
		}
		
		return searchResultId;
	}
	
	CachedSearchResult getCachedSearchResult(SearchKey searchKey) {
		
		if (searchKey == null) {
			throw new IllegalArgumentException("searchKey == null");
		}

		final SortedSearchResult searchResult;
		
		synchronized (this) {
			searchResult = sortedResultByCriteria.get(searchKey);
		}
		
		return searchResult != null
				? new CachedSearchResult(searchResult.getSearchResultId(), searchResult.getMatchCount(), searchResult.getFacetsResult())
				: null;
	}
	
	SearchItemResult [] getSearchResults(String searchResultId, int index, int count, SortOrder sortOrder) {
		
		if (searchResultId == null) {
			throw new IllegalArgumentException("searchResultId == null");
		}
		
		if (sortOrder == null) {
			throw new IllegalArgumentException("sortOrder == null");
		}
		
		final SearchItemResult [] results;
		
		final SortedSearchResult sortedSearchResult;
		
		synchronized (this) {
			sortedSearchResult = sortedResultBySearchResultId.get(searchResultId);
		}
		
		if (sortedSearchResult == null) {
			results = null;
		}
		else {
			
			results = sortedSearchResult.getSearchResults(index, count, sortOrder);
			
		}
		
		return results;
	}
}
