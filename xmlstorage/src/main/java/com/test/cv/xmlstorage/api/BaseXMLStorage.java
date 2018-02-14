package com.test.cv.xmlstorage.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import com.test.cv.common.IOUtil;
import com.test.cv.common.ItemId;
import com.test.cv.xmlstorage.api.IItemStorage.ImageResult;

public abstract class BaseXMLStorage implements IItemStorage {

	protected interface ILock {
		
	}
	
	/**
	 * List all files indices in-order. These might not be monotonically increasing
	 * since files may have been deleted
	 * @param userId
	 * @param itemId
	 * @param itemFileType
	 * @return
	 */
	private int [] listFileIndicesSorted(String userId, String itemId, ItemFileType itemFileType) {
		final String [] files = listFiles(userId, itemId, itemFileType);
		
		// file names are number#mime_type#itemId
		final int [] result = getIndicesUnsorted(files);

		Arrays.sort(result);
		
		return result;
	}
	
	private final int [] getIndicesUnsorted(String [] files) {
		final int [] result = new int[files.length];

		for (int i = 0; i < files.length; ++ i) {
			final String fileNo = getFileNameParts(files[i])[0];
			
			final int f = Integer.parseInt(fileNo);
			
			result[i] = f;
		}

		return result;
	}
	
	private String [] getFileNameParts(String fileName) {
		return fileName.split("#");
	}
	
	protected final String allocateFileName(String userId, String itemId, ItemFileType itemFileType, String mimeType) {
		final int [] indices = listFileIndicesSorted(userId, itemId, itemFileType);
		
		final int allocatedId = indices.length == 0 ? 1 : indices[indices.length - 1] + 1;
		
		return String.valueOf(allocatedId) + '#' + mimeType.replace('/', '_') + '#' + itemId;
	}

	private static final class Entry implements Comparable<Entry> {
		private final int index;
		private final String fileName;

		Entry(int index, String fileName) {
			this.index = index;
			this.fileName = fileName;
		}

		@Override
		public int compareTo(Entry o) {
			return Integer.compare(this.index, o.index);
		}
	}

	protected final Entry [] getImageFilesSorted(String userId, String itemId, ItemFileType itemFileType) {

		final String [] files = listFiles(userId, itemId, itemFileType);
		
		final int [] indices = getIndicesUnsorted(files);
		
		// Must sort files according to indices, eg swap files both files and indices
		final Entry [] entries = new Entry[files.length];
		
		for (int i = 0; i < files.length; ++ i) {
			entries[i] = new Entry(indices[i], files[i]);
		}
		
		Arrays.sort(entries);
		
		return entries;
	}

	protected final String getImageFileName(String userId, String itemId, ItemFileType itemFileType, int fileNo) {
		return getImageFilesSorted(userId, itemId, itemFileType)[fileNo].fileName;
	}
	
	protected final String getMimeTypeFromFileName(String fileName) {
		// Get mime-type by splitting on '#'
		final String mimeType = getFileNameParts(fileName)[1].replace("_", "/");
		
		return mimeType;
	}
	
	protected final void writeAndCloseOutput(InputStream inputStream, OutputStream outputStream) throws StorageException {
		
		try {
			IOUtil.copyStreams(inputStream, outputStream);
		}
		catch (IOException ex) {
			throw new StorageException("Failed to copy data", ex);
		}
		finally {
			try {
				outputStream.close();
			} catch (IOException ex) {
				throw new StorageException("Failed to close output stream", ex);
			}
		}
	}

	
	protected abstract String [] listFiles(String userId, String itemId, ItemFileType itemFileType);

	/**
	 * Lock files for this user and this item by creating a lock file or object
	 * This might block for a while if there are other ongoing operations for this userId/itemId combination
	 * @param userId
	 * @param itemId
	 */
	protected abstract ILock obtainLock(String userId, String itemId) throws StorageException;
	
	/**
	 * Release lock for userId/itemId
	 * 
	 * @param userId
	 * @param itemId
	 */
	protected abstract void releaseLock(String userId, String itemId, ILock lock);

	@Override
	public int getNumThumbnailsAndPhotosForItem(String userId, String itemId) throws StorageException {

		final ILock lock = obtainLock(userId, itemId);
	
		final String [] thumbs;
		final String [] photos;

		try {
			thumbs = listFiles(userId, itemId, ItemFileType.THUMBNAIL);
			photos = listFiles(userId, itemId, ItemFileType.PHOTO);
	
			if (thumbs.length != photos.length) {
				throw new IllegalStateException("mismatch between thumbs and photos");
			}
		}
		finally {
			releaseLock(userId, itemId, lock);
		}
		
		return thumbs.length;
	}


	protected abstract ImageResult getImageFileForItem(String userId, String itemId, ItemFileType itemFileType, String fileName) throws StorageException;
		
	@Override
	public final List<ImageResult> getThumbnailsForItem(String userId, String itemId) throws StorageException {

		final ILock lock = obtainLock(userId, itemId);
		
		final List<ImageResult> result;
		
		try {
			final Entry [] entries = getImageFilesSorted(userId, itemId, ItemFileType.THUMBNAIL);
	
			result = new ArrayList<>(entries.length);
			
			for (Entry entry : entries) {
				final ImageResult image = getImageFileForItem(userId, itemId, ItemFileType.THUMBNAIL, entry.fileName);
				
				result.add(image);
			}
		}
		finally {
			releaseLock(userId, itemId, lock);
		}
		
		return result;
	}
	
	private ImageResult getImageFileForItem(String userId, String itemId, int photoNo, ItemFileType itemFileType) throws StorageException {

		final String fileName = getImageFileName(userId, itemId, itemFileType, photoNo);
		
		return getImageFileForItem(userId, itemId, itemFileType, fileName);
	}


	@Override
	public final ImageResult getThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException {
		final ILock lock = obtainLock(userId, itemId);

		try {
			return getImageFileForItem(userId, itemId, photoNo, ItemFileType.THUMBNAIL);
		}
		finally {
			releaseLock(userId, itemId, lock);
		}
	}

	@Override
	public final ImageResult getPhotoForItem(String userId, String itemId, int photoNo) throws StorageException {
		final ILock lock = obtainLock(userId, itemId);

		try {
			return getImageFileForItem(userId, itemId, photoNo, ItemFileType.PHOTO);
		}
		finally {
			releaseLock(userId, itemId, lock);
		}
	}

	@Override
	public void retrieveThumbnails(ItemId[] itemIds, BiConsumer<ImageResult, ItemId> consumer) throws StorageException {
		// Retrieve thumbnails for all that have such
		for (ItemId itemId : itemIds) {
			if (getNumThumbnailsAndPhotosForItem(itemId.getUserId(), itemId.getItemId()) > 0) {
				
				final String fileName = getImageFileName(itemId.getUserId(), itemId.getItemId(), ItemFileType.THUMBNAIL, 0);

				final ImageResult image = getImageFileForItem(itemId.getUserId(), itemId.getItemId(), ItemFileType.THUMBNAIL, fileName);
				
				consumer.accept(image, itemId);
			}
		}
	}
}
