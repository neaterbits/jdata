package com.test.cv.xmlstorage.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	
	private File getFile(String userId) {
		return new File(baseDir, userId);
	}

	@Override
	public InputStream getCVXMLForUser(String userId) throws StorageException {
		try {
			return new FileInputStream(getFile(userId));
		} catch (FileNotFoundException ex) {
			throw new StorageException("No such file", ex);
		}
	}

	@Override
	public void storeCVXMLForUser(String userId, InputStream inputStream) throws StorageException {
		
		final byte [] buffer = new byte[10000];
		
		FileOutputStream outputStream = null;
		
		try {
			outputStream = new FileOutputStream(getFile(userId));

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
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteCVXMLForUser(String userId) throws StorageException {
		getFile(userId).delete();
	}
	
}
