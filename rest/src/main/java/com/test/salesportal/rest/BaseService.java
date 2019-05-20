package com.test.salesportal.rest;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.test.salesportal.dao.IItemDAO;
import com.test.salesportal.dao.IItemRetrieval;
import com.test.salesportal.dao.IItemUpdate;
import com.test.salesportal.dao.xml.XMLItemDAO;
import com.test.salesportal.filesystem.api.IFileSystem;
import com.test.salesportal.filesystem.local.LocalFileSystem;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.integrationtest.IntegrationTestHelper;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.model.text.Language;
import com.test.salesportal.xmlstorage.api.IItemStorage;
import com.test.salesportal.xmlstorage.filesystem.files.ParameterFileSystemFilesXMLStorage;

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
			
	protected final LocalFileSystem getLocalFileSystem() {
		return new LocalFileSystem(localBaseDir);
	}
	
	protected final IItemStorage getLocalXMLStorage() {
		final IFileSystem fileSystem = getLocalFileSystem();
		final ParameterFileSystemFilesXMLStorage localStorage = new ParameterFileSystemFilesXMLStorage(fileSystem);

		return localStorage;
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

	protected final IItemRetrieval getItemRetrievalDAO(HttpServletRequest request, ItemTypes itemTypes) {
		return getItemDAO(request, itemTypes);
	}

	protected final IItemUpdate getItemUpdateDAO(HttpServletRequest request, ItemTypes itemTypes) {
		return getItemDAO(request, itemTypes);
	}

	private final IItemDAO getItemDAO(HttpServletRequest request, ItemTypes itemTypes) {
		
		final IItemDAO ret;
		
		final Storage storage = getStorageType(request);
		
		switch (storage) {
		case LOCAL_FILE_LUCENE:
			
			ret = new XMLItemDAO(getLocalXMLStorage(), assureIndex(), itemTypes);
			break;

		default:
			throw new UnsupportedOperationException("TODO - unsupported storage " + storage);
		}
		
		return ret;
	}
}
