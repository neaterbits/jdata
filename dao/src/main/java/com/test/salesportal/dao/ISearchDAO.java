package com.test.salesportal.dao;

import java.util.List;
import java.util.Set;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.model.SortAttributeAndOrder;
import com.test.salesportal.search.criteria.Criterium;

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
	 * @param sortOrder multiple attributes at which we can sort
	 * @param facetAttributes attributes for which to return faceted results
	 * 
	 * @return a cursor for showing results, may be a long list of items
	 * 
	 * @todo add freetext
	 */
	ISearchCursor search(
			List<Class<? extends Item>> types,
			String freeText,
			List<Criterium> criteria,
			List<SortAttributeAndOrder> sortOrder,
			Set<ItemAttribute> fieldAttributes,
			Set<ItemAttribute> facetAttributes) throws SearchException;
	
}


