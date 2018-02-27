package com.test.cv.index;

import java.util.List;

import com.test.cv.model.Item;
import com.test.cv.model.ItemAttributeValue;
import com.test.cv.search.criteria.Criterium;

/**
 * Interface for index implementations in no-SQL case, eg Lucene or Elasticsearch
 */
public interface ItemIndex extends AutoCloseable {

	/**
	 * Index attributes for an item
	 * @param itemType type of item to index
	 * @param attributeValues values to index
	 */
	
	void indexItemAttributes(Class<? extends Item> itemType, List<ItemAttributeValue<?>> attributeValues) throws ItemIndexException;
	

	/**
	 * Search for values in an index based on types
	 */
	IndexSearchCursor search(String freeText, Criterium ... criteria) throws ItemIndexException;
	
	
}
