package com.test.cv.xmlstorage.api;

import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;

import com.test.cv.common.ItemId;

public interface IItemStorage {
	
	public static class ImageResult {
		public final String mimeType;
		public final int imageSize;
		public final InputStream inputStream;
		
		public ImageResult(String mimeType, int imageSize, InputStream inputStream) {
			
			if (mimeType == null) {
				throw new IllegalArgumentException("mimeType == null");
			}
			
			if (inputStream == null) {
				throw new IllegalArgumentException("inputStream == null");
			}
			
			this.mimeType = mimeType;
			this.imageSize = imageSize;
			this.inputStream = inputStream;
		}
	}

	InputStream getXMLForItem(String userId, String itemId) throws StorageException;

	void storeXMLForItem(String userId, String itemId, InputStream inputStream) throws StorageException;

	// If we delete XML, we will also delete photos and thumbnails
	void deleteAllItemFiles(String userId, String itemId) throws StorageException;

	int getNumThumbnailsAndPhotosForItem(String userId, String itemId) throws StorageException;

	// Sorted in order
	List<ImageResult> getThumbnailsForItem(String userId, String itemId) throws StorageException;
	
	ImageResult getThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException;

	ImageResult getPhotoForItem(String userId, String itemId, int photoNo) throws StorageException;

	void addPhotoAndThumbnailForItem(String userId, String itemId,
			InputStream thumbnailInputStream, String thumbnailMimeType,
			InputStream photoInputStream, String photoMimeType) throws StorageException;
	
	void deletePhotoAndThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException;

	void retrieveThumbnails(ItemId [] itemIds, BiConsumer<ImageResult, ItemId> consumer) throws StorageException;

	void movePhotoAndThumbnailForItem(String userId, String itemId, int photoNo, int toIndex) throws StorageException;
	
	int getNumFiles(String userId, String itemId, ItemFileType itemFileType) throws StorageException;
}
