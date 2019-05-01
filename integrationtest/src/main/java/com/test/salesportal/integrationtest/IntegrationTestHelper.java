package com.test.salesportal.integrationtest;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.test.salesportal.common.IOUtil;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.index.lucene.LuceneItemIndex;

public class IntegrationTestHelper {

	public static File makeBaseDir() {
		return IOUtil.makeTempFileAndDeleteOnExit("xmlitemtest");
	}
	
	public static ItemIndex makeIndex(File baseDir) {
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
