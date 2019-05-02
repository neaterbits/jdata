package com.test.salesportal.filesystem.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.test.salesportal.common.IOUtil;
import com.test.salesportal.common.StringUtil;
import com.test.salesportal.filesystem.api.IFileSystem;

public class LocalFileSystem implements IFileSystem {

	private final File baseDir;

	public LocalFileSystem(File baseDir) {

		if (!baseDir.exists() || !baseDir.isDirectory()) {
			throw new IllegalArgumentException("No directory " + baseDir);
		}
		
		this.baseDir = baseDir;
	}

	private File pathToFile(String [] path) {
		
		return new File(baseDir, StringUtil.join(path, File.separatorChar));
	}
	
	
	private void writeAndCloseOutput(InputStream inputStream, OutputStream outputStream) throws IOException {
		
		try {
			IOUtil.copyStreams(inputStream, outputStream);
		}
		catch (IOException ex) {
			throw new IOException("Failed to copy data", ex);
		}
		finally {
			try {
				outputStream.close();
			} catch (IOException ex) {
				throw new IOException("Failed to close output stream", ex);
			}
		}
	}

	@Override
	public void storeFile(String[] path, InputStream toStore, Integer streamLength) throws IOException {
		
		final File file = pathToFile(path);
		
		final File dir = file.getParentFile();
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new IOException("Failed to create item directory");
			}
		}

		writeAndCloseOutput(toStore, new FileOutputStream(file));
	}

	@Override
	public InputStream readFile(String[] path) throws FileNotFoundException {
		return new FileInputStream(pathToFile(path));
	}
	
	@Override
	public FileInput readFileInput(String[] path) throws IOException {

		final File file = pathToFile(path);
		
		return new FileInput(new FileInputStream(file), Long.valueOf(file.length()).intValue());
	}

	@Override
	public boolean exists(String[] path) {
		
		final File file = pathToFile(path);
		
		return file.exists() && file.isFile();
	}

	@Override
	public void deleteFile(String[] path) {
		pathToFile(path).delete();
	}
	
	private static final String [] NO_STRINGS = new String[0];

	@Override
	public String[] listFiles(String[] path) {
		final String [] files =  pathToFile(path).list();
	
		return files != null ? files : NO_STRINGS;
	}
}
