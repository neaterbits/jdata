package com.test.cv.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {
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
}
