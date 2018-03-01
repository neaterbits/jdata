package com.test.cv.dao.xml;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.index.IndexSearchDAO;
import com.test.cv.dao.test.SearchDAOTest;
import com.test.cv.index.ItemIndex;
import com.test.cv.index.lucene.LuceneItemIndex;
import com.test.cv.xmlstorage.local.LocalXmlStorage;

public class IndexSearchDAOTest extends SearchDAOTest {

	private final File baseDir;
	private final ItemIndex index;
	
	public IndexSearchDAOTest() {
		baseDir = XMLDAOTestHelper.makeBaseDir();
		try {
			final Directory luceneDirectory = FSDirectory.open(baseDir.toPath());
		
			index = new LuceneItemIndex(luceneDirectory);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to create lucene index", ex);
		}
	}
	
	@Override
	protected IItemDAO getItemDAO() {
		return new XMLItemDAO(new LocalXmlStorage(baseDir), index);
	}

	@Override
	protected ISearchDAO getSearchDAO() {
		return new IndexSearchDAO(index);
	}
}
