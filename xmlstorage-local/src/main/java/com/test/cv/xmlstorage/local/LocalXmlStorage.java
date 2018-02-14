package com.test.cv.xmlstorage.local;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

import com.test.cv.common.IOUtil;
import com.test.cv.xmlstorage.api.BaseXMLStorage;
import com.test.cv.xmlstorage.api.IItemStorage;
import com.test.cv.xmlstorage.api.ItemFileType;
import com.test.cv.xmlstorage.api.StorageException;

public class LocalXmlStorage extends BaseXMLStorage implements IItemStorage {

	private final File baseDir;

	public LocalXmlStorage(File baseDir) {

		if (!baseDir.exists() || !baseDir.isDirectory()) {
			throw new IllegalArgumentException("No directory " + baseDir);
		}
		
		this.baseDir = baseDir;
	}
	
	private File userDir(String userId) {
		return new File(baseDir, userId);
	}
	
	private File itemDir(String userId, String itemId) {
		return new File(userDir(userId), itemId);
	}

	private File itemFile(String userId, String itemId, ItemFileType type, String fileName) {
		return new File(new File(itemDir(userId, itemId), type.getDirectoryName()), fileName);
	}

	private File getXMLFile(String userId, String itemId) {
		return new File(itemDir(userId, itemId), "item.xml");
	}

	private File getLockFile(String userId, String itemId) {
		return new File(itemDir(userId, itemId), "lockfile");
	}

	@Override
	protected String[] listFiles(String userId, String itemId, ItemFileType itemFileType) {
		
		final File dir = new File(itemDir(userId, itemId), itemFileType.getDirectoryName());
		
		if (!dir.exists() || !dir.isDirectory()) {
			throw new IllegalStateException("No such directory : " + dir);
		}
		
		return dir.list();
	}
	
	private static class LocalFileLock implements ILock {
		private final FileLock lock;

		LocalFileLock(FileLock lock) {
			this.lock = lock;
		}
	}

	@Override
	protected ILock obtainLock(String userId, String itemId) throws StorageException {
		final File file = getLockFile(userId, itemId);
		
		final FileLock fileLock;
		
		try {
			final FileChannel fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE);
			fileLock = fileChannel.lock();
		}
		catch (IOException ex) {
			throw new StorageException("Failed to obtain file lock for " + file, ex);
		}

		return new LocalFileLock(fileLock);
	}

	@Override
	public void releaseLock(String userId, String itemId, ILock lock) {
		try {
			((LocalFileLock)lock).lock.release();
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to release lock", ex);
		}
	}

	@Override
	public InputStream getXMLForItem(String userId, String itemId) throws StorageException {
		try {
			return new FileInputStream(getXMLFile(userId, itemId));
		} catch (FileNotFoundException ex) {
			throw new StorageException("No such file", ex);
		}
	}

	@Override
	public void storeXMLForItem(String userId, String itemId, InputStream inputStream) throws StorageException {

		final File xmlFile = getXMLFile(userId, itemId);

		final File dir = xmlFile.getParentFile();
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new StorageException("Failed to create item directory");
			}
		}

		// Create thumb and photo subdirectories
		for (ItemFileType itemFileType : ItemFileType.values()) {
			final File itemFileDir = new File(itemDir(userId, itemId), itemFileType.getDirectoryName());
			
			if (!itemFileDir.mkdirs()) {
				throw new StorageException("Could not create storage directory " + itemFileDir);
			}
		}
		
		// Write a lockfile for later use
		final File lockFile = getLockFile(userId, itemId);
		try {
			writeAndCloseOutput(new ByteArrayInputStream(new byte [0]), new FileOutputStream(lockFile));
		} catch (FileNotFoundException ex) {
			throw new StorageException("Failed to write lock file to " + lockFile, ex);
		}

		boolean ok = false;
		
		try {
			writeAndCloseOutput(inputStream, new FileOutputStream(xmlFile));
			
			ok = true;
		} catch (FileNotFoundException ex) {
			throw new StorageException("Failed to open output stream " + xmlFile, ex);
		}
		finally {
			if (!ok) {
				lockFile.delete();
			}
		}
	}

	@Override
	protected ImageResult getImageFileForItem(String userId, String itemId, ItemFileType itemFileType, String fileName) throws StorageException {
		
		final File file = itemFile(userId, itemId, itemFileType, fileName);
		
		final String mimeType = getMimeTypeFromFileName(fileName);
		
		final InputStream inputStream;
		
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException ex) {
			throw new StorageException("Could not open file " + file, ex);
		}
		
		return new ImageResult(mimeType, Long.valueOf(file.length()).intValue(), inputStream);
	}
	

	@Override
	public void deleteAllItemFiles(String userId, String itemId) throws StorageException {
		
		final ILock lock = obtainLock(userId, itemId);
		
		try {
			IOUtil.deleteDirectoryRecursively(itemDir(userId, itemId));
		}
		finally {
			releaseLock(userId, itemId, lock);
		}
	}


	@Override
	public void addPhotoAndThumbnailForItem(String userId, String itemId,
			InputStream thumbnailInputStream, String thumbnailMimeType,
			InputStream photoInputStream, String photoMimeType) throws StorageException {
		
		final ILock lock = obtainLock(userId, itemId);
		
		try {
			final String thumbFileName = allocateFileName(userId, itemId, ItemFileType.THUMBNAIL, thumbnailMimeType);

			final File thumbFile = itemFile(userId, itemId, ItemFileType.THUMBNAIL, thumbFileName);
			
			writeAndCloseOutput(thumbnailInputStream, new FileOutputStream(thumbFile));
			
			boolean ok = false;
			try {
				final String photoFileName = allocateFileName(userId, itemId, ItemFileType.PHOTO, photoMimeType);

				writeAndCloseOutput(photoInputStream, new FileOutputStream(itemFile(userId, itemId, ItemFileType.PHOTO, photoFileName)));
				
				ok = true;
			}
			catch (FileNotFoundException ex) {
				throw new StorageException("Failed to open photo output file", ex);
			}
			finally {
				if (!ok) {
					thumbFile.delete();
				}
			}
		}
		catch (FileNotFoundException ex) {
			throw new StorageException("Failed to open thumb output file", ex);
		}
		finally {
			releaseLock(userId, itemId, lock);
		}
	}

	@Override
	public void deletePhotoAndThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException {
		final ILock lock = obtainLock(userId, itemId);
		
		try {
			final String thumbFileName = getImageFileName(userId, itemId, ItemFileType.THUMBNAIL, photoNo);
			final String photoFileName = getImageFileName(userId, itemId, ItemFileType.PHOTO, photoNo);
			
			itemFile(userId, itemId, ItemFileType.THUMBNAIL, thumbFileName).delete();
			itemFile(userId, itemId, ItemFileType.PHOTO, photoFileName).delete();
		}
		finally {
			releaseLock(userId, itemId, lock);
		}
	}
}
