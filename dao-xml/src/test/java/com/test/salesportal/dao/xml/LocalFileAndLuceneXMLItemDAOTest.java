package com.test.salesportal.dao.xml;

import java.io.File;

import com.test.salesportal.dao.IItemDAO;
import com.test.salesportal.dao.test.ItemDAOTest;
import com.test.salesportal.dao.xml.XMLItemDAO;
import com.test.salesportal.filesystem.local.LocalFileSystem;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.integrationtest.IntegrationTestHelper;
import com.test.salesportal.xmlstorage.api.IItemStorage;
import com.test.salesportal.xmlstorage.filesystem.files.FileSystemFilesStorage;

public class LocalFileAndLuceneXMLItemDAOTest extends ItemDAOTest {

	private final File baseDir;
	private final ItemIndex index;
	
	public LocalFileAndLuceneXMLItemDAOTest() {
		baseDir = IntegrationTestHelper.makeBaseDir();
		index = IntegrationTestHelper.makeIndex(baseDir);
	}
	
	@Override
	protected IItemDAO getItemDAO() {
		final IItemStorage localXmlStorage = new FileSystemFilesStorage(new LocalFileSystem(baseDir));

		return new XMLItemDAO(localXmlStorage, index);
	}
}
