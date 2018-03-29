package com.test.cv.dao.xml;

import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.index.IndexSearchDAO;
import com.test.cv.dao.test.SearchDAOTest;

public class ElasticSearchIndexDAOTest extends SearchDAOTest {

	@Override
	protected IItemDAO getItemDAO() {
		return S3AndElasticSearchXMLItemDAOTest.makeItemDAO();
	}

	@Override
	protected ISearchDAO getSearchDAO() {
		return new IndexSearchDAO(S3AndElasticSearchXMLItemDAOTest.makeIndex(), true);
	}
}
