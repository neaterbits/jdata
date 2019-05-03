package com.test.salesportal.xmlstorage.filesystem.files;

import com.test.salesportal.filesystem.api.IFileSystem;

public class ParameterFileSystemFilesXMLStorage extends FileSystemFilesXMLStorage {

	private final IFileSystem fileSystem;

	public ParameterFileSystemFilesXMLStorage(IFileSystem fileSystem) {

		if (fileSystem == null) {
			throw new IllegalArgumentException("fileSystem == null");
		}

		this.fileSystem = fileSystem;
	}

	@Override
	protected IFileSystem getFileSystem(String userId, String itemId) {
		return fileSystem;
	}
}
