package com.test.salesportal.filesystem.api;

import java.io.IOException;
import java.io.InputStream;

public interface IFileSystem {

	public static class FileInput {
		private final InputStream inputStream;
		private final int length;

		public FileInput(InputStream inputStream, int length) {
			this.inputStream = inputStream;
			this.length = length;
		}

		public InputStream getInputStream() {
			return inputStream;
		}

		public int getLength() {
			return length;
		}
	}
	
	void storeFile(String [] path, InputStream toStore, Integer streamLength) throws IOException;

	default InputStream readFile(String [] path) throws IOException {
		
		final FileInput fileInput = readFileInput(path);
		
		return fileInput != null ? fileInput.getInputStream() : null;
	}
	
	FileInput readFileInput(String [] path) throws IOException;

	void deleteFile(String [] path);
	
	boolean exists(String [] path);
	
	String [] listFiles(String [] path);
}
