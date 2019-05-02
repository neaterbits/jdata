package com.test.salesportal.dao.xml;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.test.salesportal.dao.IItemDAO;
import com.test.salesportal.dao.test.ItemDAOTest;
import com.test.salesportal.dao.xml.XMLItemDAO;
import com.test.salesportal.filesystem.s3.S3FileSystem;
import com.test.salesportal.index.ItemIndexException;
import com.test.salesportal.index.elasticsearch.ElasticSearchIndex;
import com.test.salesportal.index.elasticsearch.aws.AWSElasticseachIndex;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.attributes.ClassAttributes;
import com.test.salesportal.xmlstorage.filesystem.files.FileSystemFilesStorage;

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

		final S3FileSystem itemFileSystem = new S3FileSystem(
				awsCredentialsProvider,
				Regions.EU_WEST_2,
				"eltodo-testbucket");
		
		return new XMLItemDAO(new FileSystemFilesStorage(itemFileSystem), makeIndex());
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
