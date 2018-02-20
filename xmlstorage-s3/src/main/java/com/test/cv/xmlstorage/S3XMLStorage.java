package com.test.cv.xmlstorage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
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
	
	public S3XMLStorage(AWSCredentialsProvider credentialsProvider, String bucketName) {
		this.client = new AmazonS3Client(credentialsProvider);
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
		final S3Object object = client.getObject(new GetObjectRequest(bucketName, path));
	
		return object;
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
	public void storeXMLForItem(String userId, String itemId, InputStream inputStream) throws StorageException {
		
		client.putObject(new PutObjectRequest(bucketName, xmlFilePath(userId, itemId), inputStream, null));

	}

	public void deleteItemFile(String userId, String itemId, ItemFileType itemFileType, String fileName) throws StorageException {
	
		client.deleteObject(new DeleteObjectRequest(userId, getKey(userId, itemId, itemFileType, fileName)));
		
	}

	@Override
	public void deleteAllItemFiles(String userId, String itemId) throws StorageException {

		final ILock lock = obtainLock(userId, itemId);
		
		try {
			deleteDirecoryFiles(userId, itemId, ItemFileType.XML);
			deleteDirecoryFiles(userId, itemId, ItemFileType.THUMBNAIL);
			deleteDirecoryFiles(userId, itemId, ItemFileType.PHOTO);
		}
		finally {
			releaseLock(userId, itemId, lock);
			
			deleteLock(userId, itemId);
		}
		
	}

	@Override
	public void addPhotoAndThumbnailForItem(String userId, String itemId, InputStream thumbnailInputStream,
			String thumbnailMimeType, InputStream photoInputStream, String photoMimeType) throws StorageException {
		
		
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
		final ILock lock = obtainLock(userId, itemId);

		final String thumbFileName;
		final String photoFileName;

		try {
			thumbFileName = deleteImageFile(userId, itemId, ItemFileType.THUMBNAIL, photoNo);
			photoFileName = deleteImageFile(userId, itemId, ItemFileType.PHOTO, photoNo);

			removeFromImageList(userId, itemId, thumbFileName, photoFileName);
		}
		finally {
			releaseLock(userId, itemId, lock);
		}
	}

	@Override
	protected String[] listFiles(String userId, String itemId, ItemFileType itemFileType) {
		
		final String path = filesDir(userId, itemId, itemFileType);
		
		final ObjectListing listing = client.listObjects(new ListObjectsRequest(bucketName, path, null, "/", null));
		
		if (listing.isTruncated()) {
			throw new IllegalStateException("object list was truncated");
		}
		
		final int num = listing.getObjectSummaries().size();
		
		final String [] result = new String[num];
		
		for (int i = 0; i < num; ++ i) {
			result[i] = listing.getObjectSummaries().get(i).getKey();
		}
		
		return result;
	}

	@Override
	protected ILock obtainLock(String userId, String itemId) throws StorageException {

		// There is no way to lock a file in S3 safely so must use DynamoDB or similar
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	protected void releaseLock(String userId, String itemId, ILock lock) {
		throw new UnsupportedOperationException("TODO");
	}

	protected void deleteLock(String userId, String itemId) {
		throw new UnsupportedOperationException("TODO");
	}

	
	@Override
	protected InputStream getImageListInputForItem(String userId, String itemId, String fileName) throws StorageException {
		
		final String path = itemDir(userId, itemId) + '/' + fileName;
		
		final S3Object s3Object = getS3Object(path);
		
		return s3Object.getObjectContent();
	}
	

	@Override
	protected void storeImageListForItem(String userId, String itemId, String fileName, InputStream inputStream)
			throws StorageException {
		final String path = itemDir(userId, itemId) + '/' + fileName;
	
		client.putObject(new PutObjectRequest(bucketName, xmlFilePath(userId, itemId), inputStream, null));
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
