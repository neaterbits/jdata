package com.test.salesportal.dao;

import java.util.List;

import com.test.salesportal.model.items.ItemAttributeValue;
import com.test.salesportal.model.items.base.TitlePhotoItem;
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
	TitlePhotoItem getItem();
	
}
