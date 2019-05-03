package com.test.salesportal.dao.xml;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.test.salesportal.dao.IItemDAO;
import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.dao.index.IndexSearchDAO;
import com.test.salesportal.dao.test.SearchDAOTest;
import com.test.salesportal.dao.xml.XMLItemDAO;
import com.test.salesportal.filesystem.local.LocalFileSystem;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.index.lucene.LuceneItemIndex;
import com.test.salesportal.integrationtest.IntegrationTestHelper;
import com.test.salesportal.xmlstorage.api.IItemStorage;
import com.test.salesportal.xmlstorage.filesystem.files.ParameterFileSystemFilesXMLStorage;

public class LuceneIndexSearchDAOTest extends SearchDAOTest {

	private final File baseDir;
	private final ItemIndex index;
	
	public LuceneIndexSearchDAOTest() {
		baseDir = IntegrationTestHelper.makeBaseDir();
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
		final IItemStorage localXmlStorage = new ParameterFileSystemFilesXMLStorage(new LocalFileSystem(baseDir));
		return new XMLItemDAO(localXmlStorage, index);
	}

	@Override
	protected ISearchDAO getSearchDAO() {
		return new IndexSearchDAO(index, false);
	}
}
