package com.test.cv.common;

import java.io.File;

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
}
