package com.test.cv.index;

import java.util.List;

import com.test.cv.search.facets.ItemsFacets;


public interface IndexSearchCursor {

	/**
	 * Get IDs of the next items
	 * @param initialIdx
	 * @param count
	 * @return item IDs, if no more items to be found, returns < count
	 */
	List<String> getItemIDs(int initialIdx, int count);
	
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
