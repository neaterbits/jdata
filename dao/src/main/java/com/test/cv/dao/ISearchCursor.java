package com.test.cv.dao;

import java.util.List;

import com.test.cv.search.SearchItem;
import com.test.cv.search.facets.ItemsFacets;

public interface ISearchCursor {

	/**
	 * Get IDs of the next items
	 * @param initialIdx
	 * @param count
	 * @return item IDs, if no more items to be found, returns < count
	 */
	List<String> getItemIDs(int initialIdx, int count);
	
	public default List<String> getAllItemIDs() {
		return getItemIDs(0, Integer.MAX_VALUE);
	}
	
	/**
	 * Retrieve items and information
	 * 
	 * @param initialIdx
	 * @param count
	 * @return items, if no more items to be found, returns < count
	 */
	List<SearchItem> getItemIDsAndTitles(int initialIdx, int count);
	
	public default List<SearchItem> getAllItemIDsAndTitles() {
		return getItemIDsAndTitles(0, Integer.MAX_VALUE);
	}

	/**
	 * Total number of items that matched the search.
	 * Useful when displaying scrollbar.
	 * 
	 * @return total number of matches
	 */
	int getTotalMatchCount();

	/**
	 * Get facets 
	 * 
	 * @return facets
	 */
	
	ItemsFacets getFacets();
}
