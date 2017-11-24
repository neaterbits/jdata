package com.test.cv.xmlstorage;

import java.io.InputStream;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
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

	@Override
	public InputStream getCVXMLForUser(String userId) throws StorageException {

		final S3Object object = client.getObject(new GetObjectRequest(bucketName, userId + "/cv.xml"));
		
		return object.getObjectContent();
	}
}
