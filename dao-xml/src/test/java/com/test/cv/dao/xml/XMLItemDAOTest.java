package com.test.cv.dao.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.test.cv.common.IOUtil;
import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.test.ItemDAOTest;
import com.test.cv.xmlstorage.local.LocalXmlStorage;

public class XMLItemDAOTest extends ItemDAOTest {

	private final File baseDir;
	
	public XMLItemDAOTest() {
		try {
			baseDir = Files.createTempDirectory("xmlitemtest").toFile();
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to create test dir");
		}

		baseDir.mkdirs();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			IOUtil.deleteDirectoryRecursively(baseDir);
		}));
		
	}
	
	@Override
	protected IItemDAO getItemDAO() {
		return new XMLItemDAO(new LocalXmlStorage(baseDir));
	}
}
