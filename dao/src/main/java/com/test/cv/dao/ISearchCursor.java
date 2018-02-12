package com.test.cv.dao;

import java.util.List;

public interface ISearchCursor {

	/**
	 * Get IDs of the next items
	 * @param initialIdx
	 * @param count
	 * @return item IDs, if no more items to be found, returns < count
	 */
	List<String> getItemIDs(int initialIdx, int count);
	
	/**
	 * Retrieve items and information
	 * 
	 * @param initialIdx
	 * @param count
	 * @return items, if no more items to be found, returns < count
	 */
	List<IFoundItem> getItems(int initialIdx, int count);
	
}
