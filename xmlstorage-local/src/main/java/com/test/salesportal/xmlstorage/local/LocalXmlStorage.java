package com.test.salesportal.xmlstorage.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.test.salesportal.common.IOUtil;
import com.test.salesportal.xmlstorage.api.BaseXMLStorage;
import com.test.salesportal.xmlstorage.api.IItemStorage;
import com.test.salesportal.xmlstorage.api.ItemFileType;
import com.test.salesportal.xmlstorage.api.StorageException;

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

	@Override
	protected String[] listFiles(String userId, String itemId, ItemFileType itemFileType) {
		
		final File dir = new File(itemDir(userId, itemId), itemFileType.getDirectoryName());

		final String [] result;
		
		if (!dir.exists() || !dir.isDirectory()) {
			// probably was deleted
			result = new String[0];
		}
		else {
			result = dir.list();
		}
		
		
		return result;
	}
	
	@Override
	public InputStream getXMLForItem(String userId, String itemId) throws StorageException {

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(getXMLFile(userId, itemId));
		} catch (FileNotFoundException ex) {
			inputStream = null;
		}

		return inputStream;
	}
	
	@Override
	public void storeXMLForItem(String userId, String itemId, InputStream inputStream, Integer contentLength) throws StorageException {

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

		try {
			writeAndCloseOutput(inputStream, new FileOutputStream(xmlFile));
		} catch (FileNotFoundException ex) {
			throw new StorageException("Failed to open output stream " + xmlFile, ex);
		}
	}
	
	

	@Override
	protected InputStream getImageListInputForItem(String userId, String itemId, String fileName) throws StorageException {
	
		InputStream result;
		try {
			result = new FileInputStream(new File(itemDir(userId, itemId), fileName));
		} catch (FileNotFoundException ex) {
			result = null;
		}
		
		return result;
	}
	
	@Override
	protected void storeImageListForItem(String userId, String itemId, String fileName, InputStream inputStream, Integer contentLength)
			throws StorageException {
		
		final File file = new File(itemDir(userId, itemId), fileName);

		try (OutputStream outputStream = new FileOutputStream(file)) {
			writeAndCloseOutput(inputStream, outputStream);
		} catch (FileNotFoundException ex) {
			throw new StorageException("Failed to create imagelist output file", ex);
		} catch (IOException ex) {
			throw new StorageException("Exception when writing to output", ex);
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
		IOUtil.deleteDirectoryRecursively(itemDir(userId, itemId));
	}


	@Override
	public int addPhotoAndThumbnailForItem(String userId, String itemId,
			InputStream thumbnailInputStream, String thumbnailMimeType, Integer thumbLength,
			InputStream photoInputStream, String photoMimeType, Integer photoLength) throws StorageException {
		
		final int index;

		try {
			final String thumbFileName = allocateFileName(userId, itemId, ItemFileType.THUMBNAIL, thumbnailMimeType);

			final File thumbFile = itemFile(userId, itemId, ItemFileType.THUMBNAIL, thumbFileName);
			
			writeAndCloseOutput(thumbnailInputStream, new FileOutputStream(thumbFile));
			
			boolean ok = false;
			try {
				final String photoFileName = allocateFileName(userId, itemId, ItemFileType.PHOTO, photoMimeType);

				writeAndCloseOutput(photoInputStream, new FileOutputStream(itemFile(userId, itemId, ItemFileType.PHOTO, photoFileName)));

				index = addToImageList(userId, itemId, thumbFileName, thumbnailMimeType, photoFileName, photoMimeType);
				
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

		return index;
	}
	
	

	@Override
	public int addPhotoUrlAndThumbnailForItem(String userId, String itemId, InputStream thumbnailInputStream,
			String thumbnailMimeType, Integer thumbLength, String photoUrl) throws StorageException {
		final int index;

		try {
			final String thumbFileName = allocateFileName(userId, itemId, ItemFileType.THUMBNAIL, thumbnailMimeType);

			final File thumbFile = itemFile(userId, itemId, ItemFileType.THUMBNAIL, thumbFileName);
			
			writeAndCloseOutput(thumbnailInputStream, new FileOutputStream(thumbFile));
			
			boolean ok = false;
			try {
				index = addToImageList(userId, itemId, thumbFileName, thumbnailMimeType, photoUrl);
				
				ok = true;
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

		return index;
	}


	@Override
	public void deletePhotoAndThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException {
		final String thumbFileName = getImageFileName(userId, itemId, ItemFileType.THUMBNAIL, photoNo);
		final String photoFileName = getImageFileName(userId, itemId, ItemFileType.PHOTO, photoNo);
		
		itemFile(userId, itemId, ItemFileType.THUMBNAIL, thumbFileName).delete();
		itemFile(userId, itemId, ItemFileType.PHOTO, photoFileName).delete();
		
		// Remove from image as well
		removeFromImageList(userId, itemId, thumbFileName, photoFileName);
	}

	@Override
	public boolean itemExists(String userId, String itemId) {
		return itemDir(userId, itemId).exists();
	}
}
