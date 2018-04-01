package com.test.cv.index;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.test.cv.common.ItemId;
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
	
	void indexItemAttributes(String userId, Class<? extends Item> itemType, String typeName, List<ItemAttributeValue<?>> attributeValues) throws ItemIndexException;
	
	/**
	 * @param itemId item ID
	 * @param type ElasticSearch implementation requires type
	 * @param photoNo index of thumbnail, from 0 to n
	 * @param thumbWidth width of thumb as stored
	 * @param thumbHeight height of thumb as stored
	 */
	void indexThumbnailSize(String itemId, Class<? extends Item> type, int photoNo, int thumbWidth, int thumbHeight) throws ItemIndexException;

	void deletePhotoAndThumbnailForItem(String itemId, Class<? extends Item> type, int photoNo) throws ItemIndexException;

	void movePhotoAndThumbnailForItem(String itemId, Class<? extends Item> type, int photoNo, int toIndex) throws ItemIndexException;

	ItemId [] expandToItemIdUserId(String [] itemIds) throws ItemIndexException;

	/**
	 * Search for values in an index based on types
	 */
	IndexSearchCursor search(String freeText, List<Criterium> criteria, Set<ItemAttribute> facetAttributes) throws ItemIndexException;
	
	void deleteItem(String itemId, Class<? extends Item> type) throws ItemIndexException;
	
	public static String fieldName(ItemAttribute attribute) {
		return attribute.getName();
	}

	public default boolean isIdAttribute(ItemAttribute attribute) {
		return attribute.getName().equals("id");
	}
	
	public default <F, T> T [] updateThumbnailSizeArray(
			F field,
			int index, int thumbWidth, int thumbHeight,
			T defaultValue,
			Function<F, T[]> fieldToArray, Function<Integer, T[]> allocateArray, BiFunction<Integer, Integer, T> encodeSize) {
		
		T [] sizes;
		
		if (field != null) {
			sizes = fieldToArray.apply(field);
			
			final int len = sizes.length;
			if (index >= len) {
				final int newLen = index + 1;
				sizes = Arrays.copyOf(sizes, newLen);
				Arrays.fill(sizes, len, newLen, defaultValue);
			}
		}
		else {
			sizes = allocateArray.apply(index + 1);

			Arrays.fill(sizes, defaultValue);
		}
		
		sizes[index] = encodeSize.apply(thumbWidth, thumbHeight);
		
		return sizes;
	}
	
	public default <T> T [] moveThumbnail(T [] sizes, T toMove, int photoNo, int toIndex, Function<Integer, T[]> allocateArray) {
		// Use list methods for simplicity
		final List<T> sizeList = Arrays.stream(sizes).collect(Collectors.toList());

		sizeList.remove(photoNo);

		// Add at to-index
		sizeList.add(toIndex, toMove);

		return sizeList.toArray(allocateArray.apply(sizeList.size()));
	}
	
	
	public default <T> T[] deleteThumbnail(T [] sizes, int photoNo, Function<Integer, T[]> allocateArray) {
		final List<T> sizeList = Arrays.stream(sizes).collect(Collectors.toList());

		sizeList.remove(photoNo);

		return sizeList.toArray(allocateArray.apply(sizeList.size()));
	}
}
