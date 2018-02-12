package com.test.cv.dao;

import com.test.cv.dao.criteria.SearchCriteria;
import com.test.cv.model.Item;

/**
 * DAO for generic search over items in storage, returns paged results
 */

public interface ISearchDAO {

	/**
	 * Search for items using a freetext and a number for criteria to match
	 * 
	 * @param type item type
	 * @param freeText freetext to search for
	 * @param criteria list of criteria
	 * @return a cursor for sshowing results, may be a long list of items
	 * 
	 * @todo add freetext
	 */
	ISearchCursor search(Class<? extends Item> type /*, String freeText */, SearchCriteria ... criteria);

}


