package com.test.salesportal.search;

import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttribute;
import com.test.salesportal.model.items.base.ItemTypes;

/**
 * Information for one item returned from search result
 */
public interface SearchItem {
	
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

	/**
	 * Main thumbnail width
	 * 
	 * @return width
	 */

	Integer getThumbWidth();

	/**
	 * Main thumbnail height
	 * 
	 * @return height
	 */
	Integer getThumbHeight();

	/**
	 * Sort attribute values
	 */
	
	Object getSortAttributeValue(SortAttribute attribute, ItemTypes itemTypes);
	
	/**
	 * Any attribute value
	 */
	
	Object getFieldAttributeValue(ItemAttribute attribute);
}
