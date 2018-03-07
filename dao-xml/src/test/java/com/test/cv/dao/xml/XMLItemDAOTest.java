package com.test.cv.dao.xml;

import java.io.File;

import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.test.ItemDAOTest;
import com.test.cv.index.ItemIndex;
import com.test.cv.integrationtest.IntegrationTestHelper;
import com.test.cv.xmlstorage.local.LocalXmlStorage;

public class XMLItemDAOTest extends ItemDAOTest {

	private final File baseDir;
	private final ItemIndex index;
	
	public XMLItemDAOTest() {
		baseDir = IntegrationTestHelper.makeBaseDir();
		index = IntegrationTestHelper.makeIndex(baseDir);
	}
	
	@Override
	protected IItemDAO getItemDAO() {
		return new XMLItemDAO(new LocalXmlStorage(baseDir), index);
	}
}
