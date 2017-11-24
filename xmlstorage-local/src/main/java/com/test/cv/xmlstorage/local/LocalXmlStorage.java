package com.test.cv.xmlstorage.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.test.cv.xmlstorage.api.IXMLStorage;
import com.test.cv.xmlstorage.api.StorageException;

public class LocalXmlStorage implements IXMLStorage {

	private final File baseDir;

	public LocalXmlStorage(File baseDir) {

		if (!baseDir.exists() || !baseDir.isDirectory()) {
			throw new IllegalArgumentException("No directory " + baseDir);
		}
		
		this.baseDir = baseDir;
	}

	@Override
	public InputStream getCVXMLForUser(String userId) throws StorageException {
		try {
			return new FileInputStream(new File(baseDir, userId));
		} catch (FileNotFoundException ex) {
			throw new StorageException("No such file", ex);
		}
	}
}
