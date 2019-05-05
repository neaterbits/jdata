package com.test.salesportal.dao.xml;

import com.test.salesportal.dao.IItemUpdate;
import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.dao.index.IndexSearchDAO;
import com.test.salesportal.dao.test.SearchDAOTest;
import com.test.salesportal.index.ItemIndexException;

public class ElasticSearchIndexDAOTest extends SearchDAOTest {

	@Override
	protected IItemUpdate getItemDAO() {
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
