package com.test.cv.xmlstorage.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

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
	protected final int [] listFileIndicesSorted(String userId, String itemId, ItemFileType itemFileType) {
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
		
		final int allocatedId = indices.length == 0 ? 1 : indices[indices.length + 1];
		
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

	protected final String getImageFileName(String userId, String itemId, ItemFileType itemFileType, int fileNo) {

		final String [] files = listFiles(userId, itemId, itemFileType);
		
		final int [] indices = getIndicesUnsorted(files);
		
		// Must sort files according to indices, eg swap files both files and indices
		final Entry [] entries = new Entry[files.length];
		
		for (int i = 0; i < files.length; ++ i) {
			entries[i] = new Entry(indices[i], files[i]);
		}
		
		Arrays.sort(entries);
		
		return entries[fileNo].fileName;
	}
	
	protected final String getMimeTypeFromFileName(String fileName) {
		// Get mime-type by splitting on '#'
		final String mimeType = getFileNameParts(fileName)[1].replace("_", "/");
		
		return mimeType;
	}
	
	protected final void writeAndCloseOutput(InputStream inputStream, OutputStream outputStream) throws StorageException {
		final byte [] buffer = new byte[10000];
		
		
		try {

			for (;;) {
				final int bytesRead = inputStream.read(buffer);
				
				if (bytesRead < 0) {
					break;
				}
				
				outputStream.write(buffer, 0, bytesRead);
			}
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


	protected abstract ImageResult getImageFileForItem(String userId, String itemId, int photoNo, ItemFileType itemFileType, String fileName) throws StorageException;
		

	
	private ImageResult getImageFileForItem(String userId, String itemId, int photoNo, ItemFileType itemFileType) throws StorageException {

		final String [] thumbs = listFiles(userId, itemId, itemFileType);
		
		final String fileName = thumbs[photoNo];
		
		return getImageFileForItem(userId, itemId, photoNo, itemFileType, fileName);
	}

	@Override
	public final ImageResult getThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException {
		return getImageFileForItem(userId, itemId, photoNo, ItemFileType.THUMBNAIL);
	}

	@Override
	public final ImageResult getPhotoForItem(String userId, String itemId, int photoNo) throws StorageException {
		return getImageFileForItem(userId, itemId, photoNo, ItemFileType.PHOTO);
	}
}
