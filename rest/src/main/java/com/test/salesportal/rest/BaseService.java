package com.test.salesportal.rest;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.test.salesportal.dao.IItemDAO;
import com.test.salesportal.dao.IItemRetrieval;
import com.test.salesportal.dao.IItemUpdate;
import com.test.salesportal.dao.xml.XMLItemDAO;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.integrationtest.IntegrationTestHelper;
import com.test.salesportal.model.cv.Language;
import com.test.salesportal.xmlstorage.local.LocalXmlStorage;

public abstract class BaseService {
	
	public enum Storage {
		JPA_RELATIONAL, // standard relational database
		AMAZON_S3_ES, // S3 and ElasticSearch in AWS
		LOCAL_FILE_LUCENE // local file and Lucene for search
	};
	
	protected static Storage getStorageType(HttpServletRequest request) {
		// just store current storage as an attribute
		Storage storage = (Storage)request.getSession().getAttribute("storage");
		
		if (storage == null) {
			storage = Storage.LOCAL_FILE_LUCENE;
		}

		return storage;
	}

	protected static Language [] getLanguages(HttpServletRequest request) {
		
		Language language = (Language)request.getSession().getAttribute("language");
		
		if (language == null) {
			language = Language.NB_NO;
		}

		return new Language [] { language };
	}
	
	private static File localBaseDir;
			
	protected BaseService(String localFileDir) {
		
		final File f = new File(localFileDir);
		
		synchronized (BaseService.class) {
			if (localBaseDir == null) {
				BaseService.localBaseDir = f;
			}
			else {
				if (!f.equals(localBaseDir)) {
					throw new IllegalStateException("Mismatch in local base dir: " + f + "/" + localBaseDir);
				}
			}
		}
	}
			
	protected final LocalXmlStorage getLocalXMLStorage() {
		return new LocalXmlStorage(localBaseDir);
	}

	private static ItemIndex luceneIndex;

	// Lucene index must be static to avoid creating the writer multiple times
	// We synchronize this on class level
	protected synchronized static ItemIndex assureIndex() {

		final File indexDir = new File(localBaseDir, "index");

		if (luceneIndex == null) {
			luceneIndex = IntegrationTestHelper.makeIndex(indexDir);
		}
		
		return luceneIndex;
	}

	protected final IItemRetrieval getItemRetrievalDAO(HttpServletRequest request) {
		return getItemDAO(request);
	}

	protected final IItemUpdate getItemUpdateDAO(HttpServletRequest request) {
		return getItemDAO(request);
	}

	private final IItemDAO getItemDAO(HttpServletRequest request) {
		
		final IItemDAO ret;
		
		final Storage storage = getStorageType(request);
		
		switch (storage) {
		case LOCAL_FILE_LUCENE:
			final LocalXmlStorage localXmlStorage = getLocalXMLStorage();
			ret = new XMLItemDAO(localXmlStorage, assureIndex());
			break;

		default:
			throw new UnsupportedOperationException("TODO - unsupported storage " + storage);
		}
		
		return ret;
	}
}
