package com.test.cv.xmlstorage.api;

import java.io.InputStream;
import java.util.List;

public interface IItemStorage {
	
	public static class ImageResult {
		public final String mimeType;
		public final InputStream inputStream;
		
		public ImageResult(String mimeType, InputStream inputStream) {
			
			if (mimeType == null) {
				throw new IllegalArgumentException("mimeType == null");
			}
			
			if (inputStream == null) {
				throw new IllegalArgumentException("inputStream == null");
			}
			
			this.mimeType = mimeType;
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
}
