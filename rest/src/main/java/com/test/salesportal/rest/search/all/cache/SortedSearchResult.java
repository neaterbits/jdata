package com.test.salesportal.rest.search.all.cache;

import java.util.List;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.model.SortOrder;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.rest.search.all.AllSearchItemResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;

public abstract class SortedSearchResult {

	static final AllSearchItemResult [] EMPTY_ARRAY = new AllSearchItemResult[0];

	private final String searchResultId;
	
	private final List<SortAttribute> sortAttributes;
	
	private SearchFacetsResult facetsResult;
	
	abstract void insertSearchResults(AllSearchItemResult [] searchResults);
	
	abstract AllSearchItemResult [] getSearchResults(int index, int count, SortOrder sortOrder);
	
	abstract int getMatchCount();

	abstract void applyItemToCachedItems(SearchKey searchKey, Item item);
	
	abstract void deleteItemFromCachedItems(String itemId);
	
	SortedSearchResult(String searchResultId, List<SortAttribute> sortAttributes) {
		
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
	}
	
	final void initFacetsResult(SearchFacetsResult facetsResult) {
		
		if (facetsResult == null) {
			throw new IllegalArgumentException("facetsResult");
		}
		
		if (this.facetsResult != null) {
			throw new IllegalStateException("Overwriting initial update from index");
		}
		
		this.facetsResult = facetsResult;
	}
	
	final SearchFacetsResult getFacetsResult() {
		return facetsResult;
	}

	final String getSearchResultId() {
		return searchResultId;
	}

	final List<SortAttribute> getSortAttributes() {
		return sortAttributes;
	}

	final void applyItem(SearchKey searchKey, Item item, List<TypeInfo> allTypes) {
		
		// Call subclass to update result items
		applyItemToCachedItems(searchKey, item);
		
		if (facetsResult != null) {
			// Update facet counts
			SearchFacetsResultUtil.addItem(facetsResult, item, ItemTypes.getTypeInfo(item), allTypes);
		}
	}
	
	final void deleteItem(String itemId) {
		deleteItemFromCachedItems(itemId);
	}
}
