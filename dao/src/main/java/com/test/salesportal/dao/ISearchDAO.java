package com.test.salesportal.dao;

import java.util.List;
import java.util.Set;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttributeAndOrder;
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
	 * @param fieldAttributes attributes for which to return field values
	 * @param facetAttributes attributes for which to return faceted results
	 * 
	 * @return a cursor for showing results, may be a long list of items
	 * 
	 */
	ISearchCursor search(
			List<Class<? extends Item>> types,
			String freeText,
			List<Criterium> criteria,
			List<SortAttributeAndOrder> sortOrder,
			boolean returnSortAttributeValues,
			Set<ItemAttribute> fieldAttributes,
			Set<ItemAttribute> facetAttributes) throws SearchException;
	
}


