package com.test.cv.rest;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.test.cv.common.EnvVariables;
import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.xml.XMLItemDAO;
import com.test.cv.index.ItemIndex;
import com.test.cv.integrationtest.IntegrationTestHelper;
import com.test.cv.model.Item;
import com.test.cv.model.cv.Language;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.xmlstorage.local.LocalXmlStorage;

public abstract class BaseService {
	
	protected static final int THUMBNAIL_MAX_SIZE = 240;
	
	public static boolean isTest() {
		final String test = System.getenv(EnvVariables.ELTODO_LOCALHOST_TEST);

		return "true".equals(test);
	}

	enum Storage {
		JPA_RELATIONAL, // standard relational database
		AMAZON_S3_ES, // S3 and ElasticSearch in AWS
		LOCAL_FILE_LUCENE // local file and Lucene for search
	};
	
	static Storage getStorageType(HttpServletRequest request) {
		// just store current storage as an attribute
		Storage storage = (Storage)request.getSession().getAttribute("storage");
		
		if (storage == null) {
			storage = Storage.LOCAL_FILE_LUCENE;
		}

		return storage;
	}

	static Language [] getLanguages(HttpServletRequest request) {
		
		Language language = (Language)request.getSession().getAttribute("language");
		
		if (language == null) {
			language = Language.NB_NO;
		}

		return new Language [] { language };
	}
	
	private static final File localBaseDir = new File("/Users/nils.lorentzen/cvs");
	
	static LocalXmlStorage getLocalXMLStorage() {
		return new LocalXmlStorage(localBaseDir);
	}

	private static ItemIndex luceneIndex;

	// Lucene index must be static to avoid creating the writer multiple times
	// We synchronize this on class level
	synchronized static ItemIndex assureIndex() {

		final File indexDir = new File(localBaseDir, "index");

		if (luceneIndex == null) {
			luceneIndex = IntegrationTestHelper.makeIndex(indexDir);
		}
		
		return luceneIndex;
	}

	static IItemDAO getItemDAO(HttpServletRequest request) {
		
		final IItemDAO ret;
		
		final Storage storage = getStorageType(request);
		
		switch (storage) {
		case LOCAL_FILE_LUCENE:
			final LocalXmlStorage localXmlStorage = getLocalXMLStorage();
			ret = new XMLItemDAO(localXmlStorage, assureIndex(), localXmlStorage);
			break;

		default:
			throw new UnsupportedOperationException("TODO - unsupported storage " + storage);
		}
		
		return ret;
	}
	
	static String getTypeId(Class<? extends Item> type) {
		return ItemTypes.getTypeName(type);
	}
	
	static String getTypeDisplayName(Class<? extends Item> type) {
		return ItemTypes.getTypeDisplayName(type);
	}
}
