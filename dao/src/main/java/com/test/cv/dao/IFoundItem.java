package com.test.cv.dao;

import java.util.List;

import com.test.cv.model.Item;
import com.test.cv.model.ItemAttributeValue;

public interface IFoundItem {

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
	
	/**
	 * String ID to pass along to REST service
	 * 
	 * @return item ID
	 */
	String getItemId();
	
	/**
	 * Title to be displayed in listings
	 * 
	 * @return title
	 */
	String getTitle();
}
