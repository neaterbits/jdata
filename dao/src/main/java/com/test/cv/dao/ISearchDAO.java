package com.test.cv.dao;

import com.test.cv.dao.criteria.SearchCriteria;

/**
 * DAO for generic search over items in storage, returns paged results
 */

public interface ISearchDAO {

	/**
	 * Search for items using a freetext and a number for criteria to match
	 * 
	 * @param freeText
	 * @param criteria list of criteria
	 * @return a cursor for sshowing results, may be a long list of items
	 */
	ISearchCursor search(String freeText, SearchCriteria ... criteria);

}


