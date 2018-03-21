package com.test.cv.index;

import java.util.List;
import java.util.Set;

import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
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
	
	void indexItemAttributes(Class<? extends Item> itemType, String typeName, List<ItemAttributeValue<?>> attributeValues) throws ItemIndexException;
	
	/**
	 * @param itemId item ID
	 * @param index index of thumbnail, from 0 to n
	 * @param thumbWidth width of thumb as stored
	 * @param thumbHeight height of thumb as stored
	 */
	void indexThumbnailSize(String itemId, int photoNo, int thumbWidth, int thumbHeight) throws ItemIndexException;

	void deletePhotoAndThumbnailForItem(String itemId, int photoNo) throws ItemIndexException;

	void movePhotoAndThumbnailForItem(String itemId, int photoNo, int toIndex) throws ItemIndexException;

	/**
	 * Search for values in an index based on types
	 */
	IndexSearchCursor search(String freeText, List<Criterium> criteria, Set<ItemAttribute> facetAttributes) throws ItemIndexException;
	
	
}
