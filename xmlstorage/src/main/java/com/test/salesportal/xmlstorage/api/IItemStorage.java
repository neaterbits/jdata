package com.test.salesportal.xmlstorage.api;

import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;

import com.test.salesportal.common.ItemId;
import com.test.salesportal.common.images.ThumbAndImageUrls;

public interface IItemStorage extends AutoCloseable {

	public static class ImageMetaData {
		public final String mimeType;
		public final int imageSize;
		public final int width;
		public final int height;
		
		public ImageMetaData(String mimeType, int imageSize, int width, int height) {
			
			if (mimeType == null) {
				throw new IllegalArgumentException("mimeType == null");
			}
			
			this.mimeType = mimeType;
			this.imageSize = imageSize;
			this.width = width;
			this.height = height;
		}
	}

	public static class ImageResult extends ImageMetaData {
		public final InputStream inputStream;
		
		public ImageResult(String mimeType, int imageSize, InputStream inputStream) {
			super(mimeType, imageSize, -1, -1);
			
			if (inputStream == null) {
				throw new IllegalArgumentException("inputStream == null");
			}
			
			this.inputStream = inputStream;
		}
	}

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
	void retrieveThumbnails(ItemId [] itemIds, BiConsumer<ImageResult, ItemId> consumer) throws StorageException;

	void movePhotoAndThumbnailForItem(String userId, String itemId, int photoNo, int toIndex) throws StorageException;
	
	int getNumFiles(String userId, String itemId, ItemFileType itemFileType) throws StorageException;

	boolean itemExists(String userId, String itemId) throws StorageException;
}
