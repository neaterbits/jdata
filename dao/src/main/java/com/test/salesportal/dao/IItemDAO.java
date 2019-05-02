package com.test.salesportal.dao;

import java.io.InputStream;
import java.util.List;

import com.test.salesportal.common.images.ThumbAndImageUrls;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemPhoto;

/**
 * DAO for retrieving items and all their subinformation.
 * Items here are ads for a salesportal, ie. what is shown in one ad.
 * 
 * DAO contains methods for updating the ad and for updating thumbnails and photos,
 * and rearrange the order of photos and thumbnails.
 * 
 * Upon storing items, they will typically also be indexed for later search.
 * 
 * The DAO is responsible for transactionality/any necessary atomicity.
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
	 * Get complete photo, given photo no
	 * @param photoNo
	 * @return photo
	 */
	InputStream getItemPhoto(String itemId, int photoNo) throws ItemStorageException;

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
	
	void addPhotoAndThumbnailForItem(String userId, String itemId, Class<? extends Item> type,
			InputStream thumbnailInputStream, String thumbnailMimeType, Integer thumbLength, int thumbWidth, int thumbHeight, 
			InputStream photoInputStream, String photoMimeType, Integer photoLength) throws ItemStorageException;

	void addPhotoUrlAndThumbnailForItem(String userId, String itemId, Class<? extends Item> type,
			InputStream thumbnailInputStream, String thumbnailMimeType, Integer thumbLength, int thumbWidth, int thumbHeight, 
			String photoUrl) throws ItemStorageException;

	void addThumbAndPhotoUrlsForItem(String userId, String itemId, Class<? extends Item> type, ThumbAndImageUrls urls)
			throws ItemStorageException;
	
	int getPhotoCount(String itemId) throws ItemStorageException;

	// move thumbnail indices to change priority, ie update order
	// toIndex must be in the range 0 to num - 1
	void movePhotoAndThumbnailForItem(String userId, String itemId, Class<? extends Item> type, int photoNo, int toIndex) throws ItemStorageException;
	
	void deletePhotoAndThumbnailForItem(String userId, String itemId, Class<? extends Item> type, int photoNo) throws ItemStorageException;
	
	void deleteItem(String userId, String itemId, Class<? extends Item> type) throws ItemStorageException;
	
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
