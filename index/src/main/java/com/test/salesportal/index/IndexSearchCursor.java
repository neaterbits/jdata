package com.test.salesportal.index;

import java.util.List;

import com.test.salesportal.search.SearchItem;
import com.test.salesportal.search.facets.ItemsFacets;


public interface IndexSearchCursor {

	/**
	 * Get IDs of the next items
	 * @param initialIdx
	 * @param count
	 * @return item IDs, if no more items to be found, returns < count
	 */
	List<String> getItemIDs(int initialIdx, int count);
	
	
	List<SearchItem> getItemIDsAndTitles(int initialIdx, int count);

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
