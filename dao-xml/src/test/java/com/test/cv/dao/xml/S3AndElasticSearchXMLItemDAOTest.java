package com.test.cv.dao.xml;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.LockDAO;
import com.test.cv.dao.jpa.JPALockDAO;
import com.test.cv.dao.jpa.JPANames;
import com.test.cv.dao.test.ItemDAOTest;
import com.test.cv.index.elasticsearch.ElasticSearchIndex;
import com.test.cv.index.elasticsearch.aws.AWSElasticseachIndex;
import com.test.cv.model.Item;
import com.test.cv.model.attributes.ClassAttributes;
import com.test.cv.xmlstorage.S3XMLStorage;
import com.test.cv.xmlstorage.api.LockProvider;

/**
 * Test directly on S3 and ElasticSearch in the cloud
 */

public class S3AndElasticSearchXMLItemDAOTest extends ItemDAOTest {

	static ElasticSearchIndex makeIndex() {
		final AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();

		final AWSElasticseachIndex index = new AWSElasticseachIndex(
				awsCredentialsProvider,
				Regions.EU_WEST_2,
				"eltodo-es-test",
				ClassAttributes.getFromClass(Item.class).getByName("title"));

		return index;
	}
	
	static XMLItemDAO makeItemDAO() {
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
		return makeItemDAO();
	}
}
