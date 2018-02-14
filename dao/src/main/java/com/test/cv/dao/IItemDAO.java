package com.test.cv.dao;

import java.io.InputStream;
import java.util.List;

import com.test.cv.model.Item;
import com.test.cv.model.ItemPhoto;

/**
 * DAO for retrieving items and all their subinformation
 */
public interface IItemDAO extends AutoCloseable {
	
	/**
	 * Find detailed information about an item
	 */
	IFoundItem getItem(String userId, String itemId) throws ItemStorageException;
	

	/**
	 * Get all photo thumbnails for a particular item as binary data
	 * @param itemId
	 * 
	 * @return thumbnails
	 */
	List<IFoundItemPhotoThumbnail> getPhotoThumbnails(String userId, String itemId) throws ItemStorageException;
	
	/**
	 * Get complete photo, given thumbnail
	 * @param thumbnail
	 * @return photo
	 */
	ItemPhoto getItemPhoto(String userId, IFoundItemPhotoThumbnail thumbnail) throws ItemStorageException;
	
	/**
	 * Add an item to the database, does not cascade to photos
	 * 
	 * @param item the item to add
	 * 
	 * @return ID of newly added item
	 */
	String addItem(String userId, Item item) throws ItemStorageException;
	
	/**
	 * Update an item in the database, does not cascade to photos
	 * 
	 * @param item the item to update
	 */
	void updateItem(String userId, Item item) throws ItemStorageException;
	
	void addPhotoAndThumbnailForItem(String userId, String itemId,
			InputStream thumbnailInputStream, String thumbnailMimeType,
			InputStream photoInputStream, String photoMimeType) throws ItemStorageException;
	
	void deletePhotoAndThumbnailForItem(String userId, String itemId, int photoNo) throws ItemStorageException;
	
	void deleteItem(String userId, String itemId) throws ItemStorageException;
}
