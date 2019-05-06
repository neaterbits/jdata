package com.test.salesportal.rest.search.all.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.test.salesportal.common.UUIDGenerator;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.SortOrder;
import com.test.salesportal.rest.search.SearchItemResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;

/**
 * Caches in this VM results by criteria, sorted in a particular order.
 * 
 * The cache is refreshed from the update log, depending on the revision number of the
 * whole sales portal model.
 * 
 */

public final class SearchResultCache {

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
	
	public String cacheSearchResult(
			SearchKey searchKey,
			SearchFacetsResult facetsResult,
			SearchItemResult [] searchResult) {
		
		if (searchKey == null) {
			throw new IllegalArgumentException("searchKey == null");
		}
		
		final String searchResultId = UUIDGenerator.generateUUID();
		
		final SortedSearchResult sortedSearchResult = sortedSearchResultFactory.createSortedSearchResult(
				searchResultId,
				searchKey.getSortAttributes());
		
		if (facetsResult != null) {
			sortedSearchResult.initFacetsResult(facetsResult);
		}
		
		synchronized (this) {
			sortedResultByCriteria.put(searchKey, sortedSearchResult);
			sortedResultBySearchResultId.put(searchResultId, sortedSearchResult);
		}
		
		return searchResultId;
	}
	
	public CachedSearchResult getCachedSearchResult(SearchKey searchKey) {
		
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

	public void applyToAnyMatchingCachedSearchResults(Item item) {
		
		if (item == null) {
			throw new IllegalArgumentException("item == null");
		}

		final List<Map.Entry<SearchKey, SortedSearchResult>> searchResults;
		
		synchronized (this) {
			searchResults = new ArrayList<>(sortedResultByCriteria.entrySet());
		}
		
		for (Map.Entry<SearchKey, SortedSearchResult> searchResult : searchResults) {
			searchResult.getValue().applyItem(searchResult.getKey(), item);
		}
	}

	public void deleteFromCachedSearchResults(String itemId) {
		
		if (itemId == null) {
			throw new IllegalArgumentException("itemId == null");
		}
		
		final List<SortedSearchResult> searchResults;
		
		synchronized (this) {
			searchResults = new ArrayList<>(sortedResultByCriteria.values());
		}
		
		for (SortedSearchResult searchResult : searchResults) {
			searchResult.deleteItem(itemId);
		}
	}
}
