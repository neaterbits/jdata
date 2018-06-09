package com.test.cv.search;

import com.test.cv.model.ItemAttribute;

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
	 * Any attribute value
	 */
	
	Object getAttributeValue(ItemAttribute attribute);
}
