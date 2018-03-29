package com.test.cv.xmlstorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.function.BiConsumer;

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
import com.test.cv.common.ItemId;
import com.test.cv.xmlstorage.api.BaseXMLStorage;
import com.test.cv.xmlstorage.api.IItemStorage;
import com.test.cv.xmlstorage.api.ItemFileType;
import com.test.cv.xmlstorage.api.StorageException;

public class S3XMLStorage extends BaseXMLStorage implements IItemStorage {

	private final AmazonS3 client;
	private final String bucketName;
	
	public S3XMLStorage(AWSCredentialsProvider credentialsProvider, Regions region, String bucketName) {
		
		final AmazonS3ClientBuilder builder = AmazonS3Client.builder();
		
		builder.setCredentials(credentialsProvider);
		builder.setRegion(region.getName());
		
		this.client = builder.build();
		this.bucketName = bucketName;
	}
	
	private String getKey(String userId, String itemId, ItemFileType itemFileType, String fileName) {
		return filesDir(userId, itemId, itemFileType) + "/" + fileName;
	}

	private String itemDir(String userId, String itemId) {
		return userId + "/" + itemId;
	}

	private String filesDir(String userId, String itemId, ItemFileType itemFileType) {
		return userId + "/" + itemId + "/" + itemFileType.getDirectoryName();
	}
	
	private String xmlFileName(String itemId) {
		return "item.xml";
	}
	
	private String xmlFilePath(String userId, String itemId) {
		return getKey(userId, itemId, ItemFileType.XML, xmlFileName(itemId));
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
	public InputStream getXMLForItem(String userId, String itemId) throws StorageException {

		return getInputStream(xmlFilePath(userId, itemId));
		
	}

	@Override
	public void storeXMLForItem(String userId, String itemId, InputStream inputStream, Integer length) throws StorageException {

		final String path = xmlFilePath(userId, itemId);

		putObject(path, inputStream, length);
	}

	public void deleteItemFile(String userId, String itemId, ItemFileType itemFileType, String fileName) throws StorageException {
	
		final String key = getKey(userId, itemId, itemFileType, fileName);
		
		deleteObject(key);
	}

	@Override
	public void deleteAllItemFiles(String userId, String itemId) throws StorageException {
		deleteDirecoryFiles(userId, itemId, ItemFileType.XML);
		deleteDirecoryFiles(userId, itemId, ItemFileType.THUMBNAIL);
		deleteDirecoryFiles(userId, itemId, ItemFileType.PHOTO);
	}

	@Override
	public int addPhotoAndThumbnailForItem(String userId, String itemId, InputStream thumbnailInputStream,
			String thumbnailMimeType, Integer thumbLength, InputStream photoInputStream, String photoMimeType, Integer photoLength) throws StorageException {
		
		final int index;

		final String thumbFileName = allocateFileName(userId, itemId, ItemFileType.THUMBNAIL, thumbnailMimeType);

		final String thumbKey = getKey(userId, itemId, ItemFileType.THUMBNAIL, thumbFileName);
		
		putObject(thumbKey, thumbnailInputStream, thumbLength);
		
		boolean ok = false;
		try {
			final String photoFileName = allocateFileName(userId, itemId, ItemFileType.PHOTO, photoMimeType);
			
			final String photoKey = getKey(userId, itemId, ItemFileType.PHOTO, photoFileName);
			
			putObject(photoKey, photoInputStream, photoLength);

			index = addToImageList(userId, itemId, thumbFileName, thumbnailMimeType, photoFileName, photoMimeType);
			
			ok = true;
		}
		finally {
			if (!ok) {
				deleteObject(thumbKey);
			}
		}

		return index;
	}

	private void deleteDirecoryFiles(String userId, String itemId, ItemFileType itemFileType) throws StorageException {
		
		final String [] files = listFiles(userId, itemId, itemFileType);
		
		for (String file : files) {
			deleteItemFile(userId, itemId, itemFileType, file);
		}
	}

	
	private String deleteImageFile(String userId, String itemId, ItemFileType itemFileType, int fileNo) throws StorageException {

		final String fileName = getImageFileName(userId, itemId, itemFileType, fileNo);

		deleteItemFile(userId, itemId, itemFileType, fileName);
		
		return fileName;
	}

	@Override
	public void deletePhotoAndThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException {
		final String thumbFileName;
		final String photoFileName;

		thumbFileName = deleteImageFile(userId, itemId, ItemFileType.THUMBNAIL, photoNo);
		photoFileName = deleteImageFile(userId, itemId, ItemFileType.PHOTO, photoNo);

		removeFromImageList(userId, itemId, thumbFileName, photoFileName);
	}

	@Override
	protected String[] listFiles(String userId, String itemId, ItemFileType itemFileType) {
		
		final String path = filesDir(userId, itemId, itemFileType);
		
		final ObjectListing listing = client.listObjects(new ListObjectsRequest(bucketName, path + "/", null, "/", 100));
		
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

	private String getImageListPath(String userId, String itemId, String fileName) {
		return getKey(userId, itemId, ItemFileType.XML, fileName);
	}
	@Override
	protected InputStream getImageListInputForItem(String userId, String itemId, String fileName) throws StorageException {
		
		final String path = getImageListPath(userId, itemId, fileName);

		final S3Object s3Object = getS3Object(path);
		
		return s3Object != null ? s3Object.getObjectContent() : null;
	}
	

	@Override
	protected void storeImageListForItem(String userId, String itemId, String fileName, InputStream inputStream, Integer contentLength)
			throws StorageException {

		final String path = getImageListPath(userId, itemId, fileName);

		putObject(path, inputStream, contentLength);
	}

	@Override
	protected ImageResult getImageFileForItem(String userId, String itemId, ItemFileType itemFileType,
			String fileName) throws StorageException {

		final String path = getKey(userId, itemId, itemFileType, fileName);
		
		final S3Object s3Object = getS3Object(path);
		
		final InputStream inputStream = s3Object.getObjectContent();

		return new ImageResult(
				getMimeTypeFromFileName(fileName),
				Long.valueOf(s3Object.getObjectMetadata().getContentLength()).intValue(),
				inputStream);
	}

	@Override
	public void retrieveThumbnails(ItemId[] itemIds, BiConsumer<ImageResult, ItemId> consumer) throws StorageException {

		// TODO possible to get multiple objects asynchronously?
		
		super.retrieveThumbnails(itemIds, consumer);
	}

	@Override
	public boolean itemExists(String userId, String itemId) {
		throw new UnsupportedOperationException("TODO");
	}
}
