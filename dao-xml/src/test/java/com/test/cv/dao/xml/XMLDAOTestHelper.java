package com.test.cv.dao.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.test.cv.common.IOUtil;
import com.test.cv.index.ItemIndex;
import com.test.cv.index.lucene.LuceneItemIndex;

class XMLDAOTestHelper {

	static File makeBaseDir() {
		final File baseDir;
		
		try {
			baseDir = Files.createTempDirectory("xmlitemtest").toFile();
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to create test dir");
		}

		baseDir.mkdirs();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			IOUtil.deleteDirectoryRecursively(baseDir);
		}));
	
		return baseDir;
	}
	
	static ItemIndex makeIndex(File baseDir) {
		final ItemIndex index;
		
		try {
			final Directory luceneDirectory = FSDirectory.open(baseDir.toPath());
		
			index = new LuceneItemIndex(luceneDirectory);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to create lucene index", ex);
		}
		
		return index;
	}
}
