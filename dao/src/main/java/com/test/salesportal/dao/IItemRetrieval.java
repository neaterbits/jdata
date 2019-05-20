package com.test.salesportal.dao;

import java.io.InputStream;

import com.test.salesportal.model.items.base.TitlePhotoItem;
import com.test.salesportal.model.items.photo.ItemPhoto;

public interface IItemRetrieval extends AutoCloseable {
	
	/**
	 * Find detailed information about an item
	 */
	IFoundItem getItem(String userId, String itemId) throws ItemStorageException;

	TitlePhotoItem getItem(String itemId) throws ItemStorageException;

	InputStream getItemThumb(String itemId, int thumbNo) throws ItemStorageException;

	int getPhotoCount(String itemId) throws ItemStorageException;

	/**
	 * Get complete photo, given thumbnail
	 * @param thumbnail
	 * @return photo
	 */
	ItemPhoto getItemPhoto(String userId, IFoundItemPhotoThumbnail thumbnail) throws ItemStorageException;

	/**
	 * Get complete photo, given photo no
	 * @param photoNo
	 * @return photo
	 */
	InputStream getItemPhoto(String itemId, int photoNo) throws ItemStorageException;

	/**
	 * Retrieve thumbnails and concatenate them into one stream for fast retrieval of
	 * thumbnails to show in current display.
	 * 
	 * Each entry has 
	 *  - size of thumbnail in bytes as integer, 0 means no thumbnail for this item
	 *  - 0-terminated string for mimetype, empty string if no thumbnail
	 *  
	 * @param itemIds Item IDs to retrieve thumbnails for
	 * @return stream of concatenated thumbnails
	 */
	
	InputStream retrieveAndConcatenateThumbnails(String [] itemIds) throws ItemStorageException;

	// Mostly for test verification that thumbnails and photos are deleted
	int getNumThumbnails(String userId, String itemId) throws ItemStorageException;
	int getNumPhotos(String userId, String itemId) throws ItemStorageException;

}
