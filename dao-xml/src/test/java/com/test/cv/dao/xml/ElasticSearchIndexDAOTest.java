package com.test.cv.dao.xml;

import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.index.IndexSearchDAO;
import com.test.cv.dao.test.SearchDAOTest;
import com.test.cv.index.ItemIndexException;

public class ElasticSearchIndexDAOTest extends SearchDAOTest {

	@Override
	protected IItemDAO getItemDAO() {
		try {
			return S3AndElasticSearchXMLItemDAOTest.makeItemDAO();
		} catch (ItemIndexException ex) {
			throw new IllegalStateException("Failed to create index", ex);
		}
	}

	@Override
	protected ISearchDAO getSearchDAO() {
		try {
			return new IndexSearchDAO(S3AndElasticSearchXMLItemDAOTest.makeIndex(), true);
		} catch (ItemIndexException ex) {
			throw new IllegalStateException("Failed to create index", ex);
		}
	}
}
