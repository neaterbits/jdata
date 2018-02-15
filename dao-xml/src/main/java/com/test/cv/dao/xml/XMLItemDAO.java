package com.test.cv.dao.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.test.cv.common.IOUtil;
import com.test.cv.common.ItemId;
import com.test.cv.dao.IFoundItem;
import com.test.cv.dao.IFoundItemPhotoThumbnail;
import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.dao.RetrieveThumbnailsInputStream;
import com.test.cv.dao.RetrieveThumbnailsInputStream.Thumbnail;
import com.test.cv.model.Item;
import com.test.cv.model.ItemPhoto;
import com.test.cv.model.ItemPhotoCategory;
import com.test.cv.model.items.Snowboard;
import com.test.cv.xmlstorage.api.IItemStorage;
import com.test.cv.xmlstorage.api.ItemFileType;
import com.test.cv.xmlstorage.api.IItemStorage.ImageResult;
import com.test.cv.xmlstorage.api.StorageException;

public class XMLItemDAO extends XMLBaseDAO implements IItemDAO {

	private static final JAXBContext jaxbContext;

	static {
		try {
			jaxbContext = JAXBContext.newInstance(Item.class, Snowboard.class);
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to initialize JAXB context", ex);
		}
	}

	public XMLItemDAO(IItemStorage xmlStorage) {
		super(jaxbContext, xmlStorage);
	}

	@Override
	public IFoundItem getItem(String userId, String itemId) throws ItemStorageException {
		// ID is a file name
		
		final IFoundItem found;
		
		try (InputStream inputStream = xmlStorage.getXMLForItem(userId, itemId)) {
		
			final Item item = (Item)unmarshaller.unmarshal(inputStream);

			found = new XMLFoundItem(item, itemId);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Exception on close", ex);
		}
		catch (StorageException ex) {
			throw new ItemStorageException("Failed to retrieve item", ex);
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to unmarshall XML for ID " + itemId, ex);
		}

		return found;
	}

	@Override
	public List<IFoundItemPhotoThumbnail> getPhotoThumbnails(String userId, String itemId) throws ItemStorageException {

		// Thumbnails are stored as separate files, list directory
		List<ImageResult> images;
		final List<IFoundItemPhotoThumbnail> result;
		
		try {
			images = xmlStorage.getThumbnailsForItem(userId, itemId);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to get thumbnails for " + itemId, ex);
		}
		
		try {
			result = new ArrayList<>(images.size());
			
			for (int i = 0; i < images.size(); ++ i) {
				final ImageResult image = images.get(i);
				
				final byte [] data = IOUtil.readAll(image.inputStream);
				final String id = String.valueOf(i);
				
				// TODO categories
				final List<ItemPhotoCategory> categories = new ArrayList<>();

				result.add(new XMLFoundItemPhotoThumbnail(id, itemId, i, image.mimeType, categories, data));
			}
		}
		catch (IOException ex) {
			throw new ItemStorageException("Failed to retrieve thumbnail image", ex);
		}
		finally {
			for (ImageResult image : images) {
				try {
					image.inputStream.close();
				} catch (IOException e) {
				}
			}
		}

		return result;
	}

	@Override
	public ItemPhoto getItemPhoto(String userId, IFoundItemPhotoThumbnail thumbnail) throws ItemStorageException {

		final int photoNo = Integer.parseInt(thumbnail.getId());
		
		final String itemId = ((XMLFoundItemPhotoThumbnail)thumbnail).getItemId();
		
		final ImageResult image;
		try {
			image = xmlStorage.getPhotoForItem(userId, itemId, photoNo);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to get photo", ex);
		}
		
		final ItemPhoto itemPhoto;
		
		try {
			itemPhoto = new ItemPhoto();
		
			itemPhoto.setMimeType(image.mimeType);
			itemPhoto.setData(IOUtil.readAll(image.inputStream));
		}
		catch (IOException ex) {
			throw new ItemStorageException("Failed to read photo", ex);
		}
		finally {
			try {
				image.inputStream.close();
			} catch (IOException e) {
			}
		}
		
		return itemPhoto;
	}

	@Override
	public String addItem(String userId, Item item) throws ItemStorageException {
		// Add to storage, must make an ID for item
		// Just generate an uuid
		
		final String itemId = genItemId();
		
		try {
			store(userId, itemId, item);
		} catch (XMLStorageException ex) {
			throw new ItemStorageException("Failed to store item", ex);
		}
		
		return itemId;
	}

	@Override
	public void updateItem(String userId, Item item) {
		throw new UnsupportedOperationException("TODO");
	}
	

	@Override
	public void addPhotoAndThumbnailForItem(String userId, String itemId, InputStream thumbnailInputStream,
			String thumbnailMimeType, InputStream photoInputStream, String photoMimeType) throws ItemStorageException {
		try {
			xmlStorage.addPhotoAndThumbnailForItem(userId, itemId, thumbnailInputStream, thumbnailMimeType, photoInputStream, photoMimeType);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to store thumbnail", ex);
		}
	}
	
	@Override
	public void movePhotoAndThumbnailForItem(String userId, String itemId, int photoNo, int toIndex)
			throws ItemStorageException {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void deletePhotoAndThumbnailForItem(String userId, String itemId, int photoNo) throws ItemStorageException {
		try {
			xmlStorage.deletePhotoAndThumbnailForItem(userId, itemId, photoNo);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to delete photo and thumbnail", ex);
		}
	}

	@Override
	public void deleteItem(String userId, String itemId) throws ItemStorageException {
		try {
			xmlStorage.deleteAllItemFiles(userId, itemId);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to delete item files", ex);
		}
	}

	@Override
	public void close() throws Exception {
		
	}
	
	@Override
	public InputStream retrieveAndConcatenateThumbnails(ItemId[] itemIds) throws ItemStorageException {
		
		// Retrieve thumbnails across user IDs from storage. Pass in all since might retreieve in parallell
		final Map<String, Integer> map = makeItemIdToIndexMap(itemIds);
		
		final List<Thumbnail> sorted = new ArrayList<>(itemIds.length);
		
		// default to no thumbnail
		for (int i = 0; i < itemIds.length; ++ i) {
			sorted.add(new Thumbnail("", 0, null));
		}
		
		try {
			xmlStorage.retrieveThumbnails(itemIds, (imageResult, itemId) -> {
				final int index = map.get(itemId.getItemId());
				
				sorted.set(index, new Thumbnail(imageResult.mimeType, imageResult.imageSize, imageResult.inputStream));
			});
		}
		catch (StorageException ex) {
			throw new ItemStorageException("Failed to retrieve thumbnails form storage", ex);
		}

		final Iterator<Thumbnail> sortedIterator = sorted.iterator();
		
		return new RetrieveThumbnailsInputStream() {
			
			@Override
			protected Thumbnail getNext() {
				return sortedIterator.hasNext() ? sortedIterator.next() : null;
			}
		};
	}

	private String genItemId() {
		return UUID.randomUUID().toString();
	}

	@Override
	public int getNumThumbnails(String userId, String itemId) throws ItemStorageException {
		try {
			return xmlStorage.getNumFiles(userId, itemId, ItemFileType.THUMBNAIL);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to get number of files", ex);
		}
	}

	@Override
	public int getNumPhotos(String userId, String itemId) throws ItemStorageException {
		try {
			return xmlStorage.getNumFiles(userId, itemId, ItemFileType.PHOTO);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to get number of files", ex);
		}
	}
}
