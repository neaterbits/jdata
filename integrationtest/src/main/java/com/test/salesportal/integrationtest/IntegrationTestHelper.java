package com.test.salesportal.integrationtest;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.test.salesportal.common.IOUtil;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.index.lucene.LuceneItemIndex;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.model.items.sales.SalesItemTypes;

public class IntegrationTestHelper {

	protected static final ItemTypes ITEM_TYPES = SalesItemTypes.INSTANCE;

	public static File makeBaseDir() {
		return IOUtil.makeTempFileAndDeleteOnExit("xmlitemtest");
	}
	
	public static ItemIndex makeIndex(File baseDir) {
		final ItemIndex index;
		
		try {
			final Directory luceneDirectory = FSDirectory.open(baseDir.toPath());
		
			index = new LuceneItemIndex(luceneDirectory, ITEM_TYPES);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to create lucene index", ex);
		}
		
		return index;
	}
}
