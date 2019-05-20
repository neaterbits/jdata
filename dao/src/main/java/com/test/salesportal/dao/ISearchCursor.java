package com.test.salesportal.dao;

import java.util.Collections;
import java.util.List;

import com.test.salesportal.search.SearchItem;
import com.test.salesportal.search.facets.ItemsFacets;

public interface ISearchCursor {

	public static ISearchCursor emptyCursor() {
		return new ISearchCursor() {
			
			@Override
			public int getTotalMatchCount() {
				return 0;
			}
			
			@Override
			public List<SearchItem> getItemIDsAndTitles(int initialIdx, int count) {
				return Collections.emptyList();
			}
			
			@Override
			public List<String> getItemIDs(int initialIdx, int count) {
				return Collections.emptyList();
			}
			
			@Override
			public ItemsFacets getFacets() {
				return new ItemsFacets(Collections.emptyList());
			}
		};
	}
	
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
