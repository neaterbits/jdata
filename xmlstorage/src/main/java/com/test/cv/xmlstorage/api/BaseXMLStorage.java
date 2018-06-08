package com.test.cv.xmlstorage.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.test.cv.common.IOUtil;
import com.test.cv.common.ItemId;
import com.test.cv.common.StringUtil;
import com.test.cv.common.images.ThumbAndImageUrl;
import com.test.cv.common.images.ThumbAndImageUrls;
import com.test.cv.xmlstorage.model.images.Image;
import com.test.cv.xmlstorage.model.images.ImageData;
import com.test.cv.xmlstorage.model.images.Images;

public abstract class BaseXMLStorage implements IItemStorage {

	// TODO we cannot really store this as a file in S3 since does no read-after=write consistency on update
	// would have to pass in former order on move operation
	static final String IMAGE_LIST_FILENAME = "images.xml";
	
	private static final char FILE_NAME_SEPARATOR = '.';

	private static final JAXBContext imagesContext;
	
	static {
		try {
			imagesContext = JAXBContext.newInstance(Images.class);
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to init imagelist JAXB context", ex);
		}
	}
	
	/**
	 * List all files indices in-order. These might not be monotonically increasing
	 * since files may have been deleted
	 * @param userId
	 * @param itemId
	 * @param itemFileType
	 * @return
	 */
	private int [] listFileIndicesSorted(String userId, String itemId, ItemFileType itemFileType) {
		final String [] files = listFiles(userId, itemId, itemFileType);
		
		// file names are number.mime_type.itemId
		final int [] result = getIndicesUnsorted(files);

		Arrays.sort(result);
		
		return result;
	}
	
	private final int [] getIndicesUnsorted(String [] files) {
		final int [] result = new int[files.length];

		for (int i = 0; i < files.length; ++ i) {
			final String fileNo = getFileNameParts(files[i])[0];
			
			final int f = Integer.parseInt(fileNo);
			
			result[i] = f;
		}

		return result;
	}
	
	private String [] getFileNameParts(String fileName) {
		// Cannot split with String.split() since FILE_NAME_SEPARATOR might be regex reserved char
		return StringUtil.split(fileName, FILE_NAME_SEPARATOR);
	}
	
	protected final String allocateFileName(String userId, String itemId, ItemFileType itemFileType, String mimeType) {
		final int [] indices = listFileIndicesSorted(userId, itemId, itemFileType);
		
		final int allocatedId = indices.length == 0 ? 1 : indices[indices.length - 1] + 1;
		
		return String.valueOf(allocatedId) + FILE_NAME_SEPARATOR + mimeType.replace('/', '_') + FILE_NAME_SEPARATOR + itemId;
	}

	protected final ImageData getImageData(Images images, int fileNo, ItemFileType itemFileType) {
		final Image image = images.getImages().get(fileNo);
		
		final ImageData imageData;
		
		switch (itemFileType) {
		case THUMBNAIL:
			imageData = image.getThumb();
			break;
			
		case PHOTO:
			imageData = image.getPhoto();
			break;
			
		default:
			throw new IllegalArgumentException("Unknown item file type " + itemFileType);
		}
		
		return imageData;
	}
	
	private final String getImageFileName(String userId, String itemId, ItemFileType itemFileType, Images images, int fileNo) {
		
		return getImageData(images, fileNo, itemFileType).getFileName();
	}

	protected final String getImageFileName(String userId, String itemId, ItemFileType itemFileType, int fileNo) throws StorageException {
		return getImageData(getImageList(userId, itemId), fileNo, itemFileType).getFileName();
	}

	protected final String getMimeTypeFromFileName(String fileName) {
		// Get mime-type by splitting on '.'
		final String mimeType = getFileNameParts(fileName)[1].replace("_", "/");
		
		return mimeType;
	}
	
	protected final void writeAndCloseOutput(InputStream inputStream, OutputStream outputStream) throws StorageException {
		
		try {
			IOUtil.copyStreams(inputStream, outputStream);
		}
		catch (IOException ex) {
			throw new StorageException("Failed to copy data", ex);
		}
		finally {
			try {
				outputStream.close();
			} catch (IOException ex) {
				throw new StorageException("Failed to close output stream", ex);
			}
		}
	}

	
	protected abstract String [] listFiles(String userId, String itemId, ItemFileType itemFileType);

	

	@Override
	public int getNumThumbnailsForItem(String userId, String itemId) throws StorageException {
		final String [] thumbs;

		thumbs = listFiles(userId, itemId, ItemFileType.THUMBNAIL);
		
		return thumbs.length;
	}

	@Override
	public int getNumThumbnailsAndPhotosForItem(String userId, String itemId) throws StorageException {

		final String [] thumbs;
		final String [] photos;

		thumbs = listFiles(userId, itemId, ItemFileType.THUMBNAIL);
		photos = listFiles(userId, itemId, ItemFileType.PHOTO);

		if (thumbs.length != photos.length) {
			throw new IllegalStateException("mismatch between thumbs and photos");
		}
		
		return thumbs.length;
	}

	@Override
	public final void addThumbAndPhotoUrlsForItem(String userId, String itemId, ThumbAndImageUrls urls)
			throws StorageException {

		// Only stores image-list, not any images
		final Images imageList = getOrCreateImageList(userId, itemId);

		for (ThumbAndImageUrl url : urls.getUrls()) {
			final Image image = new Image();

			image.setId(itemId);

			image.setThumbUrl(url.getThumbUrl());
			image.setPhotoUrl(url.getImageUrl());

			imageList.getImages().add(image);
		}

		writeImageList(userId, itemId, imageList);
	}

	private Images getOrCreateImageList(String userId, String itemId) throws StorageException {
		Images images = getImageList(userId, itemId);
		
		if (images == null || images.getImages() == null) {
			images = new Images();
			
			images.setImages(new ArrayList<>());
			
			writeImageList(userId, itemId, images);
		}
		
		return images;
	}
	
	private void writeImageList(String userId, String itemId, Images images) throws StorageException{
		final Marshaller mashaller;
		try {
			mashaller = imagesContext.createMarshaller();
		} catch (JAXBException ex) {
			throw new StorageException("Failed to create image list marshaller", ex);
		}
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				mashaller.marshal(images, baos);
				
				final byte [] bytes = baos.toByteArray();
	
				final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
	
				storeImageListForItem(userId, itemId, IMAGE_LIST_FILENAME, inputStream, bytes.length);
			} catch (JAXBException ex) {
			throw new StorageException("Failed to marshal image list file", ex);
		} catch (IOException ex) {
			throw new StorageException("Exception while outputing to image list file", ex);
		}

	}
	
	private int addToImageList(String userId, String itemId, Consumer<Image> init) throws StorageException {
		final Images imageList = getOrCreateImageList(userId, itemId);
		
		final Image image = new Image();
		
		image.setId(itemId);
		
		init.accept(image);
		
		imageList.getImages().add(image);

		writeImageList(userId, itemId, imageList);
		
		return imageList.getImages().size() - 1;
	}
	
	
	protected final int addToImageList(String userId, String itemId,
			String thumbnailFileName, String thumbnailMimeType,
			String photoFileName, String photoMimeType) throws StorageException {
		
		return addToImageList(userId, itemId, image -> {
			image.setThumb(makeImageData(thumbnailFileName, thumbnailMimeType));
			image.setPhoto(makeImageData(photoFileName, photoMimeType));
		});
	}

	protected final int addToImageList(String userId, String itemId,
			String thumbnailFileName, String thumbnailMimeType,
			String photoUrl) throws StorageException {
		
		return addToImageList(userId, itemId, image -> {
			image.setThumb(makeImageData(thumbnailFileName, thumbnailMimeType));
			image.setPhotoUrl(photoUrl);
		});
	}

	protected final void removeFromImageList(String userId, String itemId,
				String thumbnailFileName, String photoFileName) throws StorageException {

		final Images imageList = getImageList(userId, itemId);
		
		imageList.getImages().removeIf(image -> image.getId().equals(itemId)
				&& image.getThumb().getFileName().equals(thumbnailFileName)
				&& image.getPhoto().getFileName().equals(photoFileName));

		writeImageList(userId, itemId, imageList);
	}

	private Images getImageList(String userId, String itemId) throws StorageException {
		final Unmarshaller unmarshaller;
		try {
			unmarshaller = imagesContext.createUnmarshaller();
		} catch (JAXBException ex) {
			throw new StorageException("Failed to create image list unmarshaller", ex);
		}

		final Images images;

		InputStream inputStream = getImageListInputForItem(userId, itemId, IMAGE_LIST_FILENAME);
		
		if (inputStream == null) {
			images = null;
		}
		else {
			try {
				images = (Images)unmarshaller.unmarshal(inputStream);
			} catch (JAXBException ex) {
				throw new StorageException("Failed to parse image list", ex);
			}
			finally {
				try {
					inputStream.close();
				} catch (IOException ex) {
					throw new StorageException("Failed to close input stream", ex);
				}
			}
		}

		return images;
	}
	
	

	@Override
	public final void movePhotoAndThumbnailForItem(String userId, String itemId, int photoNo, int toIndex) throws StorageException {

		final Images imageList = getImageList(userId, itemId);
		
		if (photoNo == toIndex) {
			throw new IllegalArgumentException("Moving to same pos");
		}

		final Image toMove = imageList.getImages().get(photoNo);

		imageList.getImages().remove(photoNo);

		// Add at to-index
		imageList.getImages().add(toIndex, toMove);
		
		
		writeImageList(userId, itemId, imageList);
	}

	protected abstract InputStream getImageListInputForItem(String userId, String itemId, String fileName) throws StorageException;

	protected abstract void storeImageListForItem(String userId, String itemId, String fileName, InputStream inputStream, Integer contentLength) throws StorageException;

	protected abstract ImageResult getImageFileForItem(String userId, String itemId, ItemFileType itemFileType, String fileName) throws StorageException;
	
	
	@Override
	public final List<ImageResult> getThumbnailsForItem(String userId, String itemId) throws StorageException {

		final List<ImageResult> result;
		
		final Images images = getOrCreateImageList(userId, itemId);
		
		int fileNo = 0;
		
		result = images != null ? new ArrayList<>(images.getImages().size()) : Collections.emptyList();
		
		for (fileNo = 0; fileNo < images.getImages().size(); ++ fileNo) {

			final String fileName = getImageFileName(userId, itemId, ItemFileType.THUMBNAIL, images, fileNo);
		
			final ImageResult image = getImageFileForItem(userId, itemId, ItemFileType.THUMBNAIL, fileName);
			
			result.add(image);
		}
		
		return result;
	}
	
	private ImageResult getImageFileForItem(String userId, String itemId, int photoNo, ItemFileType itemFileType) throws StorageException {

		final Images images = getImageList(userId, itemId);

		final String fileName = getImageFileName(userId, itemId, itemFileType, images, photoNo);
		
		return getImageFileForItem(userId, itemId, itemFileType, fileName);
	}


	@Override
	public final ImageMetaData getThumbnailMetaDataForItem(String userId, String itemId, int photoNo) throws StorageException {
		final ImageMetaData metaData;
		
		final Images imageList = getImageList(userId, itemId);
		
		if (imageList == null || imageList.getImages() == null || imageList.getImages().size() <= photoNo) {
			metaData = null;
		}
		else {
			final ImageData imageData = imageList.getImages().get(photoNo).getThumb();
			
			metaData = new ImageMetaData(imageData.getMimeType(), imageData.getSize(), imageData.getWidth(), imageData.getHeight());
		}

		return metaData;
	}

	@Override
	public final ImageResult getThumbnailForItem(String userId, String itemId, int photoNo) throws StorageException {
		return getImageFileForItem(userId, itemId, photoNo, ItemFileType.THUMBNAIL);
	}

	@Override
	public final ImageResult getPhotoForItem(String userId, String itemId, int photoNo) throws StorageException {
		return getImageFileForItem(userId, itemId, photoNo, ItemFileType.PHOTO);
	}

	@Override
	public void retrieveThumbnails(ItemId[] itemIds, BiConsumer<ImageResult, ItemId> consumer) throws StorageException {
		// Retrieve thumbnails for all that have such
		for (ItemId itemId : itemIds) {
			if (getNumThumbnailsForItem(itemId.getUserId(), itemId.getItemId()) > 0) {
				
				final Images imageList = getImageList(itemId.getUserId(), itemId.getItemId());
				
				if (imageList == null) {
					throw new IllegalStateException("Should have had image list");
				}
				
				final String fileName = getImageFileName(itemId.getUserId(), itemId.getItemId(), ItemFileType.THUMBNAIL, imageList, 0);

				final ImageResult image = getImageFileForItem(itemId.getUserId(), itemId.getItemId(), ItemFileType.THUMBNAIL, fileName);
				
				consumer.accept(image, itemId);
			}
		}
	}

	@Override
	public int getNumFiles(String userId, String itemId, ItemFileType itemFileType) throws StorageException {
		return listFiles(userId, itemId, itemFileType).length;
	}
	
	protected final ImageData makeImageData(String fileName, String mimeType) {
		final ImageData imageData = new ImageData();
		
		imageData.setFileName(fileName);
		imageData.setMimeType(mimeType);
		
		return imageData;
	}
}
