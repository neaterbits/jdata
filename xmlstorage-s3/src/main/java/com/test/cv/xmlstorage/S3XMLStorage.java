package com.test.cv.xmlstorage;

import java.io.InputStream;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.test.cv.xmlstorage.api.IXMLStorage;
import com.test.cv.xmlstorage.api.StorageException;

public class S3XMLStorage implements IXMLStorage {

	private final AmazonS3 client;
	private final String bucketName;
	
	public S3XMLStorage(AWSCredentialsProvider credentialsProvider, String bucketName) {
		this.client = new AmazonS3Client(credentialsProvider);
		this.bucketName = bucketName;
	}
	
	private String getKey(String userId) {
		return userId + "/cv.xml";
	}

	@Override
	public InputStream getCVXMLForUser(String userId) throws StorageException {

		final S3Object object = client.getObject(new GetObjectRequest(bucketName, getKey(userId)));
		
		return object.getObjectContent();
	}

	@Override
	public void storeCVXMLForUser(String userId, InputStream inputStream) throws StorageException {
		
		client.putObject(new PutObjectRequest(bucketName, getKey(userId), inputStream, null));

	}

	@Override
	public void deleteCVXMLForUser(String userId) throws StorageException {
	
		client.deleteObject(new DeleteObjectRequest(userId, getKey(userId)));
		
	}
}
