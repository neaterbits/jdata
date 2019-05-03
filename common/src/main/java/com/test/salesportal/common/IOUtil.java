package com.test.salesportal.common;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;

public class IOUtil {
	
	public static File makeTempFileAndDeleteOnExit(String baseName) {
		final File baseDir;
		
		try {
			baseDir = Files.createTempDirectory(baseName).toFile();
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to create test dir");
		}

		baseDir.mkdirs();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			IOUtil.deleteDirectoryRecursively(baseDir);
		}));
	
		return baseDir;
	}
	
	public static void deleteDirectoryRecursively(File dir) {
		
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("file is not a directory: " + dir);
		}
		
		final File [] subs = dir.listFiles();
		
		for (File sub : subs) {
			
			if (sub.isDirectory()) {
				deleteDirectoryRecursively(sub);
			}
			else {
				sub.delete();
			}
		}
		
		dir.delete();
	}
	
	public static void copyStreams(InputStream inputStream, OutputStream outputStream) throws IOException {
		final byte [] buffer = new byte[10000];

		for (;;) {
			final int bytesRead = inputStream.read(buffer);
			
			if (bytesRead < 0) {
				break;
			}
			
			outputStream.write(buffer, 0, bytesRead);
		}
	}
	
	public static byte [] readAll(InputStream inputStream) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		copyStreams(inputStream, baos);

		return baos.toByteArray();
	}

	public static byte [] readAllAndClose(InputStream inputStream) throws IOException {
		
		try {
			return readAll(inputStream);
		}
		finally {
			inputStream.close();
		}
	}

	public static String readFileToString(File file) throws IOException {
		final byte [] data;
		
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			data = IOUtil.readAll(inputStream);
		}
		
		return new String(data);
	}

	public static String readAll(Reader reader) throws IOException {

		final StringBuilder sb = new StringBuilder();
		
		final char [] buf = new char[10000];
		for (;;) {
			final int charsRead = reader.read(buf);
			
			if (charsRead < 0) {
				break;
			}
			
			sb.append(buf, 0, charsRead);
		}

		return sb.toString();
	}

}
