package com.test.cv.dao;

import java.util.List;

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
	
}
