package com.test.salesportal.dao.xml;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.test.salesportal.dao.IItemDAO;
import com.test.salesportal.dao.LockDAO;
import com.test.salesportal.dao.jpa.JPALockDAO;
import com.test.salesportal.dao.jpa.JPANames;
import com.test.salesportal.dao.test.ItemDAOTest;
import com.test.salesportal.dao.xml.DAOLockProvider;
import com.test.salesportal.dao.xml.XMLItemDAO;
import com.test.salesportal.index.ItemIndexException;
import com.test.salesportal.index.elasticsearch.ElasticSearchIndex;
import com.test.salesportal.index.elasticsearch.aws.AWSElasticseachIndex;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.attributes.ClassAttributes;
import com.test.salesportal.xmlstorage.S3XMLStorage;
import com.test.salesportal.xmlstorage.api.LockProvider;

/**
 * Test directly on S3 and ElasticSearch in the cloud
 */

public class S3AndElasticSearchXMLItemDAOTest extends ItemDAOTest {

	static ElasticSearchIndex makeIndex() throws ItemIndexException {
		final AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();

		final AWSElasticseachIndex index = new AWSElasticseachIndex(
				awsCredentialsProvider,
				Regions.EU_WEST_2,
				"eltodo-es-test",
				ClassAttributes.getFromClass(Item.class).getByName("title"));

		return index;
	}
	
	static XMLItemDAO makeItemDAO() throws ItemIndexException {
		final AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();

		final S3XMLStorage itemStorage = new S3XMLStorage(
				awsCredentialsProvider,
				Regions.EU_WEST_2,
				"eltodo-testbucket");
		
		// Lock on item IDs in database
		final LockDAO lockDAO = new JPALockDAO(JPANames.PERSISTENCE_UNIT_DERBY);
		final LockProvider lockProvider = new DAOLockProvider(lockDAO);
		
		return new XMLItemDAO(itemStorage, makeIndex(), lockProvider);
	}
	
	@Override
	protected IItemDAO getItemDAO() {
		try {
			return makeItemDAO();
		} catch (ItemIndexException ex) {
			throw new IllegalStateException("Failed to create index", ex);
		}
	}
}
