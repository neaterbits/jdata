package com.test.salesportal.xmlstorage.api;

import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;

import com.test.salesportal.common.ItemId;
import com.test.salesportal.common.images.ThumbAndImageUrls;

public interface IItemStorage extends AutoCloseable {

	InputStream getXMLForItem(String userId, String itemId) throws StorageException;

	void storeXMLForItem(String userId, String itemId, InputStream inputStream, Integer contentLength) throws StorageException;

	// If we delete XML, we will also delete photos and thumbnails
	void deleteAllItemFiles(String userId, String itemId) throws StorageException;

	int getNumThumbnailsAndPhotosForItem(String userId, String itemId) throws StorageException;

	int getNumThumbnailFilesForItem(String userId, String itemId) throws StorageException;

	// Sorted in order
	List<ImageResult> getThumbnailsForItem(String userId, String itemId) throws StorageException;

	ImageMetaData getThumbnailMetaDataForItem(String userId, String itemId, int photoNo) throws StorageException;

	ImageResult getThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException;

	ImageResult getPhotoForItem(String userId, String itemId, int photoNo) throws StorageException;

	int addPhotoAndThumbnailForItem(String userId, String itemId,
			InputStream thumbnailInputStream, String thumbnailMimeType, Integer thumbLength,
			InputStream photoInputStream, String photoMimeType, Integer photoLength) throws StorageException;

	int addPhotoUrlAndThumbnailForItem(String userId, String itemId,
			InputStream thumbnailInputStream, String thumbnailMimeType, Integer thumbLength,
			String photoUrl) throws StorageException;

	void addThumbAndPhotoUrlsForItem(String userId, String itemId,
			ThumbAndImageUrls urls) throws StorageException;
	
	int getPhotoCount(String userId, String itemId) throws StorageException;
	
	void deletePhotoAndThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException;

	/**
	 * Retrieve thumbnails for all item IDs specified.
	 * 
	 * Does not return until callback called with all available item IDs.
	 * 
	 * @param itemIds
	 * @param consumer
	 * @throws StorageException
	 */
	
	@FunctionalInterface
	public interface OnThumbnails {
		void onThumbnails(ImageResult imageResult, ItemId itemId, int numItemThumbnails);
	}
	
	void retrieveThumbnails(ItemId [] itemIds, BiConsumer<ImageResult, ItemId> consumer) throws StorageException;

	void retrieveThumbnailsWithCount(ItemId [] itemIds, OnThumbnails consumer) throws StorageException;
	
	void movePhotoAndThumbnailForItem(String userId, String itemId, int photoNo, int toIndex) throws StorageException;
	
	int getNumFiles(String userId, String itemId, ItemFileType itemFileType) throws StorageException;

	boolean itemExists(String userId, String itemId) throws StorageException;
}
