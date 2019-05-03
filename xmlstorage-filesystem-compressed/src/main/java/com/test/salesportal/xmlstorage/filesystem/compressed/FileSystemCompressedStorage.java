package com.test.salesportal.xmlstorage.filesystem.compressed;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import com.test.salesportal.filesystem.api.IFileSystem;
import com.test.salesportal.filesystem.zip.ZipFileSystem;
import com.test.salesportal.xmlstorage.api.IItemStorage;
import com.test.salesportal.xmlstorage.api.StorageException;
import com.test.salesportal.xmlstorage.filesystem.files.FileSystemFilesXMLStorage;

public class FileSystemCompressedStorage extends FileSystemFilesXMLStorage implements IItemStorage {

	private final IFileSystem fileSystem;

	public FileSystemCompressedStorage(IFileSystem fileSystem) {

		if (fileSystem == null) {
			throw new IllegalArgumentException("fileSystem == null");
		}
		
		this.fileSystem = fileSystem;
	}


	@Override
	protected IFileSystem getXmlFileSystem(String userId, String itemId) throws StorageException {
		return getCompressedFileSystem(userId, itemId);
	}
	
	@Override
	protected IFileSystem getLargePhotosFileSystem(String userId, String itemId) throws StorageException {
		return fileSystem;
	}

	private IFileSystem getCompressedFileSystem(String userId, String itemId) throws StorageException {
		
		
		final String [] path = new String [] { userId, itemId, "data.zip" };
		
		try {
			if (!fileSystem.exists(path)) {

				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				try (ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {
				}
				
				final byte [] bytes = baos.toByteArray();
				
				fileSystem.storeFile(path, new ByteArrayInputStream(bytes), bytes.length);
			}
		}
		catch (IOException ex) {
			throw new StorageException("Failed to create initial zip file", ex);
		}
		
		return new ZipFileSystem(
				() -> fileSystem.readFile(path),
				bytes -> fileSystem.writeFile(path, bytes));
	}
}
