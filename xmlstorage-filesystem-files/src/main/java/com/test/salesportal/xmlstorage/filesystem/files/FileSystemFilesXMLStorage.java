package com.test.salesportal.xmlstorage.filesystem.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.test.salesportal.filesystem.api.IFileSystem;
import com.test.salesportal.filesystem.api.IFileSystem.FileInput;
import com.test.salesportal.xmlstorage.api.BaseXMLStorage;
import com.test.salesportal.xmlstorage.api.IItemStorage;
import com.test.salesportal.xmlstorage.api.ItemFileType;
import com.test.salesportal.xmlstorage.api.StorageException;

public abstract class FileSystemFilesXMLStorage extends BaseXMLStorage implements IItemStorage {

	/*
	private File userDir(String userId) {
		return new File(baseDir, userId);
	}

	private File itemDir(String userId, String itemId) {
		return new File(userDir(userId), itemId);
	}
	*/

	private String [] itemFile(String userId, String itemId, ItemFileType type, String fileName) {
		return new String [] { userId, itemId, type.getDirectoryName(), fileName };
	}

	private String [] getXMLFile(String userId, String itemId) {
		return itemFile(userId, itemId, ItemFileType.XML, "item.xml");
	}
	
	protected abstract IFileSystem getXmlFileSystem(String userId, String itemId) throws StorageException;
	protected abstract IFileSystem getLargePhotosFileSystem(String userId, String itemId) throws StorageException;

	private IFileSystem getFileSystemForItemFileType(String userId, String itemId, ItemFileType itemFileType) throws StorageException {
		return itemFileType == ItemFileType.PHOTO
				? getLargePhotosFileSystem(userId, itemId)
				: getXmlFileSystem(userId, itemId);
	}
	
	@Override
	protected final String[] listFiles(String userId, String itemId, ItemFileType itemFileType) throws StorageException {

		final String [] path = new String [] { userId, itemId, itemFileType.getDirectoryName() };
		
		try {
			return getFileSystemForItemFileType(userId, itemId, itemFileType).listFiles(path);
			
		} catch (IOException ex) {
			throw new StorageException("", ex);
		}
	}

	@Override
	public final InputStream getXMLForItem(String userId, String itemId) throws StorageException {

		InputStream inputStream = null;
		try {
			inputStream = getXmlFileSystem(userId, itemId).readFile(getXMLFile(userId, itemId));
		} catch (FileNotFoundException ex) {
			inputStream = null;
		}
		catch (IOException ex) {
			throw new StorageException("Failed to list files", ex);
		}

		return inputStream;
	}

	@Override
	public final void storeXMLForItem(String userId, String itemId, InputStream inputStream, Integer contentLength)
			throws StorageException {

		final String [] xmlFile = getXMLFile(userId, itemId);

		try {
			getXmlFileSystem(userId, itemId).storeFile(xmlFile, inputStream, contentLength);
		} catch (FileNotFoundException ex) {
			throw new StorageException("Failed to open output stream " + xmlFile, ex);
		} catch (IOException ex) {
			throw new StorageException("Failed to store file", ex);
		}
	}

	@Override
	protected final InputStream getImageListInputForItem(String userId, String itemId, String fileName)
			throws StorageException {

		InputStream result;
		try {
			result = getXmlFileSystem(userId, itemId).readFile(new String [] { userId, itemId, fileName });
		} catch (FileNotFoundException ex) {
			result = null;
		} catch (IOException ex) {
			throw new StorageException("Failed to get image list", ex);
		}

		return result;
	}

	@Override
	protected final void storeImageListForItem(String userId, String itemId, String fileName, InputStream inputStream,
			Integer contentLength) throws StorageException {

		final String [] path = new String[] { userId, itemId, fileName };

		try {
			getXmlFileSystem(userId, itemId).storeFile(path, inputStream, contentLength);
		} catch (FileNotFoundException ex) {
			throw new StorageException("Failed to create imagelist output file", ex);
		} catch (IOException ex) {
			throw new StorageException("Exception when writing to output", ex);
		}
	}

	@Override
	protected final ImageResult getImageFileForItem(String userId, String itemId, ItemFileType itemFileType, String fileName)
			throws StorageException {

		final String [] path = itemFile(userId, itemId, itemFileType, fileName);

		final String mimeType = getMimeTypeFromFileName(fileName);

		final FileInput input;

		try {
			input = getFileSystemForItemFileType(userId, itemId, itemFileType).readFileInput(path);
		} catch (FileNotFoundException ex) {
			throw new StorageException("Could not open file " + Arrays.toString(path), ex);
		} catch (IOException ex) {
			throw new StorageException("Could not read file " + Arrays.toString(path), ex);
		}

		return new ImageResult(mimeType, input.getLength(), input.getInputStream());
	}

	private void deleteItemFile(String userId, String itemId, ItemFileType itemFileType, String fileName) throws StorageException {
		
		final String [] path = itemFile(userId, itemId, itemFileType, fileName);

		try {
			getFileSystemForItemFileType(userId, itemId, itemFileType).deleteFile(path);
		} catch (IOException ex) {
			throw new StorageException("Failed to delete file", ex);
		}
	}

	private void deleteDirecoryFiles(String userId, String itemId, ItemFileType itemFileType) throws StorageException {
		
		final String [] files = listFiles(userId, itemId, itemFileType);
		
		for (String file : files) {
			deleteItemFile(userId, itemId, itemFileType, file);
		}
	}
	
	@Override
	public final void deleteAllItemFiles(String userId, String itemId) throws StorageException {
		deleteDirecoryFiles(userId, itemId, ItemFileType.XML);
		deleteDirecoryFiles(userId, itemId, ItemFileType.THUMBNAIL);
		deleteDirecoryFiles(userId, itemId, ItemFileType.PHOTO);
	}

	@Override
	public final int addPhotoAndThumbnailForItem(String userId, String itemId, InputStream thumbnailInputStream,
			String thumbnailMimeType, Integer thumbLength, InputStream photoInputStream, String photoMimeType,
			Integer photoLength) throws StorageException {

		final int index;

		try {
			
			final ItemFileType thumbFileType = ItemFileType.THUMBNAIL;
			
			final String thumbFileName = allocateFileName(userId, itemId, thumbFileType, thumbnailMimeType);

			final String [] thumbFile = itemFile(userId, itemId, thumbFileType, thumbFileName);

			getFileSystemForItemFileType(userId, itemId, thumbFileType).storeFile(thumbFile, thumbnailInputStream, thumbLength);
			
			boolean ok = false;
			try {
				
				final ItemFileType photoFileType = ItemFileType.PHOTO;
				
				final String photoFileName = allocateFileName(userId, itemId, photoFileType, photoMimeType);

				final String [] photoPath = itemFile(userId, itemId, photoFileType, photoFileName);
				
				getFileSystemForItemFileType(userId, itemId, photoFileType).storeFile(photoPath, photoInputStream, photoLength);

				index = addToImageList(userId, itemId, thumbFileName, thumbnailMimeType, photoFileName, photoMimeType);

				ok = true;
			} catch (FileNotFoundException ex) {
				throw new StorageException("Failed to open photo output file", ex);
			} catch (IOException ex) {
				throw new StorageException("Failed to store output file", ex);
			} finally {
				if (!ok) {
					getFileSystemForItemFileType(userId, itemId, thumbFileType).deleteFile(thumbFile);
				}
			}
		} catch (FileNotFoundException ex) {
			throw new StorageException("Failed to open thumb output file", ex);
		} catch (IOException ex) {
			throw new StorageException("Failed to store output file", ex);
		}

		return index;
	}

	@Override
	public final int addPhotoUrlAndThumbnailForItem(String userId, String itemId, InputStream thumbnailInputStream,
			String thumbnailMimeType, Integer thumbLength, String photoUrl) throws StorageException {
		final int index;

		try {
			final ItemFileType thumbFileType = ItemFileType.THUMBNAIL;
			final String thumbFileName = allocateFileName(userId, itemId, thumbFileType, thumbnailMimeType);

			final String [] thumbFile = itemFile(userId, itemId, thumbFileType, thumbFileName);

			getFileSystemForItemFileType(userId, itemId, thumbFileType).storeFile(thumbFile, thumbnailInputStream, thumbLength);

			boolean ok = false;
			try {
				index = addToImageList(userId, itemId, thumbFileName, thumbnailMimeType, photoUrl);

				ok = true;
			} finally {
				if (!ok) {
					getFileSystemForItemFileType(userId, itemId, thumbFileType).deleteFile(thumbFile);
				}
			}
		} catch (FileNotFoundException ex) {
			throw new StorageException("Failed to open thumb output file", ex);
		} catch (IOException ex) {
			throw new StorageException("Failed to store thumb file", ex);
		}

		return index;
	}

	@Override
	public final void deletePhotoAndThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException {
		
		final ItemFileType thumbFileType = ItemFileType.THUMBNAIL;
		final ItemFileType photoFileType = ItemFileType.PHOTO;
		
		final String thumbFileName = getImageFileName(userId, itemId, thumbFileType, photoNo);
		final String photoFileName = getImageFileName(userId, itemId, photoFileType, photoNo);

		try {
			try {
				getFileSystemForItemFileType(userId, itemId, thumbFileType).deleteFile(itemFile(userId, thumbFileName, thumbFileType, thumbFileName));
			}
			finally {
				getFileSystemForItemFileType(userId, itemId, photoFileType).deleteFile(itemFile(userId, itemId, photoFileType, photoFileName));
			}
		}
		catch (IOException ex) {
			throw new StorageException("Failed to delete file", ex);
		}
		finally {

			// Remove from image as well
			removeFromImageList(userId, itemId, thumbFileName, photoFileName);
		}
	}

	@Override
	public final boolean itemExists(String userId, String itemId) throws StorageException {
		try {
			return getXmlFileSystem(userId, itemId).exists(getXMLFile(userId, itemId));
		} catch (IOException ex) {
			throw new StorageException("Exception whie checking whether file exists", ex);
		}
	}
}
