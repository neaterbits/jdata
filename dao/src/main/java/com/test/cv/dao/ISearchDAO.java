package com.test.cv.dao;

import java.util.List;

import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.search.criteria.Criterium;

/**
 * DAO for generic search over items in storage, returns paged results
 */

public interface ISearchDAO extends AutoCloseable {

	/**
	 * Search for items using a freetext and a number for criteria to match
	 * 
	 * @param type item type
	 * @param freeText freetext to search for
	 * @param criteria list of criteria
	 * @param facetAttributes attributes for which to return faceted results
	 * 
	 * @return a cursor for showing results, may be a long list of items
	 * 
	 * @todo add freetext
	 */
	ISearchCursor search(
			List<Class<? extends Item>> types
			/*, String freeText */,
			List<Criterium> criteria,
			List<ItemAttribute> facetAttributes) throws SearchException;
	
}


