package com.test.salesportal.xmlstorage.filesystem.files;

import com.test.salesportal.filesystem.api.IFileSystem;
import com.test.salesportal.xmlstorage.api.StorageException;

public class ParameterFileSystemFilesXMLStorage extends FileSystemFilesXMLStorage {

	private final IFileSystem fileSystem;

	public ParameterFileSystemFilesXMLStorage(IFileSystem fileSystem) {

		if (fileSystem == null) {
			throw new IllegalArgumentException("fileSystem == null");
		}

		this.fileSystem = fileSystem;
	}

	@Override
	protected IFileSystem getXmlFileSystem(String userId, String itemId) {
		return fileSystem;
	}

	@Override
	protected IFileSystem getLargePhotosFileSystem(String userId, String itemId) throws StorageException {
		return fileSystem;
	}
}
