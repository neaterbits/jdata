package com.test.salesportal.filesystem.s3;

import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.test.salesportal.common.StringUtil;
import com.test.salesportal.filesystem.api.IFileSystem;

public class S3FileSystem implements IFileSystem {

	private final AmazonS3 client;
	private final String bucketName;
	
	public S3FileSystem(AWSCredentialsProvider credentialsProvider, Regions region, String bucketName) {
		
		final AmazonS3ClientBuilder builder = AmazonS3Client.builder();
		
		builder.setCredentials(credentialsProvider);
		builder.setRegion(region.getName());
		
		this.client = builder.build();
		this.bucketName = bucketName;
	}
	
	private static String pathToString(String [] path) {
		return StringUtil.join(path, '/');
	}

	private S3Object getS3Object(String path) {
		S3Object object;

		try {
			object = client.getObject(new GetObjectRequest(bucketName, path));
		}
		catch (AmazonS3Exception ex) {
			
			if (ex.getErrorCode().equals("NoSuchKey")) {
				object = null;
			}
			else throw ex;
		}

		return object;
	}

	private void putObject(String path, InputStream inputStream, Integer contentLength) {
		final ObjectMetadata metaData;
		
		if (contentLength != null) {
			metaData = new ObjectMetadata();
			metaData.setContentLength(contentLength);
		}
		else {
			metaData = null;
		}

		final PutObjectRequest request = new PutObjectRequest(bucketName, path, inputStream, metaData);
		
		client.putObject(request);
	}
	
	private void deleteObject(String key) {
		client.deleteObject(new DeleteObjectRequest(this.bucketName, key));
	}

	private InputStream getInputStream(String path) {
		final S3Object object = client.getObject(new GetObjectRequest(bucketName, path));
		
		return object.getObjectContent();
	}


	@Override
	public void storeFile(String[] path, InputStream toStore, Integer streamLength) {
		putObject(pathToString(path), toStore, streamLength);
	}

	@Override
	public InputStream readFile(String[] path) {
		return getInputStream(pathToString(path));
	}
	
	@Override
	public FileInput readFileInput(String[] path) throws IOException {

		final S3Object s3Object = getS3Object(pathToString(path));

		return s3Object != null
				? new FileInput(
						s3Object.getObjectContent(),
						Long.valueOf(s3Object.getObjectMetadata().getContentLength()).intValue())
				: null;
	}

	@Override
	public void deleteFile(String[] path) {
		
		deleteObject(pathToString(path));
	}

	@Override
	public boolean exists(String[] path) {
		return getS3Object(pathToString(path)) != null;
	}

	@Override
	public String[] listFiles(String[] path) {
		
		final String pathString = pathToString(path);
		
		final ObjectListing listing = client.listObjects(new ListObjectsRequest(bucketName, pathString + "/", null, "/", 100));
		
		if (listing.isTruncated()) {
			throw new IllegalStateException("object list was truncated");
		}
		
		final int num = listing.getObjectSummaries().size();
		
		final String [] result = new String[num];
		
		for (int i = 0; i < num; ++ i) {
			final String key = listing.getObjectSummaries().get(i).getKey();
			
			final String fileName;
			
			if (key.contains("/")) {
				final String s[] = key.split("/");
				
				fileName = s[s.length - 1];
			}
			else {
				fileName = key;
			}
			
			result[i] = fileName;
		}
		
		return result;
	}
	
	
}
