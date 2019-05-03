package com.test.salesportal.filesystem.api;

import java.io.ByteArrayInputStream;
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

	default void writeFile(String [] path, byte [] bytes) throws IOException {
		storeFile(path, new ByteArrayInputStream(bytes), bytes.length);
	}
	
	default InputStream readFile(String [] path) throws IOException {
		
		final FileInput fileInput = readFileInput(path);
		
		return fileInput != null ? fileInput.getInputStream() : null;
	}
	
	FileInput readFileInput(String [] path) throws IOException;

	void deleteFile(String [] path) throws IOException;
	
	boolean exists(String [] path) throws IOException;
	
	String [] listFiles(String [] path) throws IOException;
}
