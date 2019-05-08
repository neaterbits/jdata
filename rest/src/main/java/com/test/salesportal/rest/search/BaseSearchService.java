package com.test.salesportal.rest.search;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.dao.index.IndexSearchDAO;
import com.test.salesportal.integrationtest.IntegrationTestHelper;
import com.test.salesportal.rest.BaseService;
import com.test.salesportal.rest.search.paged.ItemSearchResult;

/**
 * Utility methods for search
 */
public abstract class BaseSearchService<
	ITEM extends SearchItemResult,
	RESULT extends ItemSearchResult<ITEM>> extends BaseService {

	protected BaseSearchService(String localFileDir) {
		super(localFileDir);
	}
	
	protected final ISearchDAO getSearchDAO(HttpServletRequest request) {
		
		final ISearchDAO ret;
		
		final Storage storage = getStorageType(request);
		
		switch (storage) {
		case LOCAL_FILE_LUCENE:
			File baseDir = (File)request.getSession().getAttribute("baseDir");
			
			if (baseDir == null) {
				baseDir = IntegrationTestHelper.makeBaseDir();
				
				request.getSession().setAttribute("baseDir", baseDir);
			}
			
			ret = new IndexSearchDAO(assureIndex(), false);
			break;
			
		default:
			throw new UnsupportedOperationException("TODO - unsupported storage " + storage);
		}
		
		return ret;
	}
}
