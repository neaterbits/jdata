package com.test.cv.dao;

import java.util.List;

import com.test.cv.model.Item;
import com.test.cv.model.ItemAttributeValue;
import com.test.cv.search.SearchItem;

public interface IFoundItem extends SearchItem {

	/**
	 * Retrieve all attributes as generic list
	 * 
	 * @return list of attribute values
	 */
	List<ItemAttributeValue<?>> getAttributes();
	
	/**
	 * Get the item as a POJO
	 * 
	 * @return item POJO
	 */
	Item getItem();
	
}
