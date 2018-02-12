package com.test.cv.dao;

import java.util.List;

import com.test.cv.model.Item;
import com.test.cv.model.ItemPhoto;

/**
 * DAO for retrieving items and all their subinformation
 */
public interface IItemDAO {
	
	/**
	 * Find detailed information about an item
	 */
	IFoundItem getItem(String id);
	

	/**
	 * Get all photo thumbnails for a particular item as binary data
	 * @param itemId
	 * 
	 * @return thumbnails
	 */
	List<IFoundItemPhotoThumbnail> getPhotoThumbnails(String itemId);
	
	/**
	 * Get complete photo, given thumbnail
	 * @param thumbnail
	 * @return photo
	 */
	ItemPhoto getItemPhoto(IFoundItemPhotoThumbnail thumbnail);
	
	/**
	 * Add an item to the database, does not cascade to photos
	 * 
	 * @param item the item to add
	 */
	void addItem(Item item);
	
	/**
	 * Update an item in the database, does not cascade to photos
	 * 
	 * @param item the item to update
	 */
	void updateItem(Item item);
}
