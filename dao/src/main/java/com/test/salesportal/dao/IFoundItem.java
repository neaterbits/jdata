package com.test.salesportal.dao;

import java.util.List;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttributeValue;
import com.test.salesportal.search.SearchItem;

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
