package com.test.salesportal.rest.search.all.cache;

import java.util.List;

import com.test.salesportal.model.items.SortAttribute;
import com.test.salesportal.model.items.SortOrder;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.model.items.base.TitlePhotoItem;
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

	abstract void applyItemToCachedItems(SearchKey searchKey, TitlePhotoItem item, ItemTypes itemTypes);
	
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
	
	final void initResult(SearchFacetsResult facetsResult, AllSearchItemResult [] items) {
		
		if (facetsResult == null) {
			throw new IllegalArgumentException("facetsResult");
		}
		
		if (this.facetsResult != null) {
			throw new IllegalStateException("Overwriting initial update from index");
		}
		
		this.facetsResult = facetsResult;
		
		insertSearchResults(items);
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

	final void applyItem(SearchKey searchKey, TitlePhotoItem item, List<TypeInfo> allTypes, ItemTypes itemTypes) {
		
		// Call subclass to update result items
		applyItemToCachedItems(searchKey, item, itemTypes);
		
		if (facetsResult != null) {
			// Update facet counts
			SearchFacetsResultUtil.addItem(facetsResult, item, itemTypes.getTypeInfo(item), allTypes);
		}
	}
	
	final void deleteItem(String itemId) {
		deleteItemFromCachedItems(itemId);
	}
}
