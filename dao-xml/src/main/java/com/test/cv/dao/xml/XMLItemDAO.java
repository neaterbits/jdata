package com.test.cv.dao.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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
import com.test.cv.index.ItemIndex;
import com.test.cv.index.ItemIndexException;
import com.test.cv.model.Item;
import com.test.cv.model.ItemPhoto;
import com.test.cv.model.ItemPhotoCategory;
import com.test.cv.model.attributes.ClassAttributes;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.xmlstorage.api.IItemStorage;
import com.test.cv.xmlstorage.api.IItemStorage.ImageMetaData;
import com.test.cv.xmlstorage.api.IItemStorage.ImageResult;
import com.test.cv.xmlstorage.api.ItemFileType;
import com.test.cv.xmlstorage.api.LockProvider;
import com.test.cv.xmlstorage.api.LockProvider.Lock;
import com.test.cv.xmlstorage.api.LockProvider.LockException;
import com.test.cv.xmlstorage.api.StorageException;

public class XMLItemDAO extends XMLBaseDAO implements IItemDAO {

	private static final JAXBContext jaxbContext;

	static {
		try {
			jaxbContext = JAXBContext.newInstance(ItemTypes.getJAXBTypeClasses());
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to initialize JAXB context", ex);
		}
	}
	
	private final LockProvider lockProvider;
	
	private Lock obtainLock(String userId, String itemId) throws ItemStorageException {
		try {
			return lockProvider.obtainLock(userId, itemId);
		} catch (LockException ex) {
			throw new ItemStorageException("Failed to obtain lock", ex);
		}
	}
	
	private void releaseLock(Lock lock) {
		lockProvider.releaseLock(lock);
	}

	public XMLItemDAO(IItemStorage xmlStorage, ItemIndex index, LockProvider lockProvider) {
		super(jaxbContext, xmlStorage, index);
		
		this.lockProvider = lockProvider;
	}

	@Override
	public IFoundItem getItem(String userId, String itemId) throws ItemStorageException {
		// ID is a file name
		
		final Lock lock = obtainLock(userId, itemId);
		
		final IFoundItem found;
		
		try {
			
			InputStream inputStream;
			try {
				inputStream = xmlStorage.getXMLForItem(userId, itemId);
			} catch (StorageException ex) {
				throw new ItemStorageException("Failed to retrieve item", ex);
			}
			
			if (inputStream == null) {
				found = null;
			}
			else {
				try {
				
					final Item item = (Item)unmarshaller.unmarshal(inputStream);
					
					final ImageMetaData thumb = xmlStorage.getThumbnailMetaDataForItem(userId, itemId, 0);
		
					found = new XMLFoundItem(
							item,
							itemId,
							thumb != null ? thumb.width : null,
							thumb != null ? thumb.height : null);
	
				} catch (JAXBException ex) {
					throw new IllegalStateException("Failed to unmarshall XML for ID " + itemId, ex);
				}
				catch (StorageException ex) {
					throw new ItemStorageException("Failed to get thumb metadata", ex);
				}
				finally {
					try {
						inputStream.close();
					}
					catch (IOException ex) {
						throw new IllegalStateException("Exception on close", ex);
					}
				}
			}
		}
		finally {
			releaseLock(lock);
		}

		return found;
	}

	@Override
	public List<IFoundItemPhotoThumbnail> getPhotoThumbnails(String userId, String itemId) throws ItemStorageException {

		final Lock lock = obtainLock(userId, itemId);
		
		List<IFoundItemPhotoThumbnail> result = null;

		try {
			// Thumbnails are stored as separate files, list directory
			List<ImageResult> images = null;
			
			try {
				images = xmlStorage.getThumbnailsForItem(userId, itemId);
			} catch (StorageException ex) {
				if (!xmlStorage.itemExists(userId, itemId)) {
					result = Collections.emptyList();
				}
				else {
					throw new ItemStorageException("Failed to get thumbnails for " + itemId, ex);
				}
			}
			
			if (result == null) {
				try {
					result = new ArrayList<>(images.size());
					
					for (int i = 0; i < images.size(); ++ i) {
						final ImageResult image = images.get(i);
						
						final byte [] data = IOUtil.readAll(image.inputStream);
						final String id = String.valueOf(i);
						
						// TODO categories
						final List<ItemPhotoCategory> categories = new ArrayList<>();
		
						result.add(new XMLFoundItemPhotoThumbnail(id, itemId, i, image.mimeType, image.width, image.height, categories, data));
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
			}
		}
		finally {
			releaseLock(lock);
		}

		return result;
	}

	@Override
	public ItemPhoto getItemPhoto(String userId, IFoundItemPhotoThumbnail thumbnail) throws ItemStorageException {

		final ItemPhoto itemPhoto;
		final String itemId = ((XMLFoundItemPhotoThumbnail)thumbnail).getItemId();
		
		final Lock lock = obtainLock(userId, itemId);
		
		try {
			final int photoNo = Integer.parseInt(thumbnail.getId());
			
			final ImageResult image;
			try {
				image = xmlStorage.getPhotoForItem(userId, itemId, photoNo);
			} catch (StorageException ex) {
				throw new ItemStorageException("Failed to get photo", ex);
			}
			
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
		}
		finally {
			releaseLock(lock);
		}
		
		return itemPhoto;
	}

	@Override
	public String addItem(String userId, Item item) throws ItemStorageException {

		// Add to storage, must make an ID for item
		// Just generate an uuid
		
		final String itemId = genItemId();
		
		item.setIdString(itemId);
		
		try {
			lockProvider.createLock(userId, itemId);
		} catch (LockException ex) {
			throw new ItemStorageException("Failed to create lock for " + itemId, ex);
		}
		
		// Also lock during creation ? Ought not be necessary since not returned itemId
		// so no other caller can access it yet. but perhaps useful as a test that locking works for this item
		final Lock lock = obtainLock(userId, itemId);
		
		try {
			try {
				store(userId, itemId, item, ItemTypes.getType(item), ClassAttributes.getValues(item));
			} catch (XMLStorageException ex) {
				throw new ItemStorageException("Failed to store item", ex);
			}
		}
		finally {
			releaseLock(lock);
		}
		
		return itemId;
	}

	@Override
	public void updateItem(String userId, Item item) {
		throw new UnsupportedOperationException("TODO");
	}
	

	@Override
	public void addPhotoAndThumbnailForItem(String userId, String itemId, Class<? extends Item> type, InputStream thumbnailInputStream,
			String thumbnailMimeType, Integer thumbLength, int thumbWidth, int thumbHeight,
			InputStream photoInputStream, String photoMimeType, Integer photoLength) throws ItemStorageException {

		final Lock lock = obtainLock(userId, itemId);
		
		try {
			final int photoNo = xmlStorage.addPhotoAndThumbnailForItem(
					userId, itemId,
					thumbnailInputStream, thumbnailMimeType, thumbLength,
					photoInputStream, photoMimeType, photoLength);

			index.indexThumbnailSize(itemId, type, photoNo, thumbWidth, thumbHeight);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to store thumbnail", ex);
		} catch (ItemIndexException ex) {
			throw new ItemStorageException("Failed to index thumbnail sizes", ex);
		}
		finally {
			releaseLock(lock);
		}
	}
	
	

	@Override
	public void addPhotoUrlAndThumbnailForItem(String userId, String itemId, Class<? extends Item> type,
			InputStream thumbnailInputStream, String thumbnailMimeType, Integer thumbLength, int thumbWidth,
			int thumbHeight, String photoUrl) throws ItemStorageException {

		final Lock lock = obtainLock(userId, itemId);
		
		try {
			final int photoNo = xmlStorage.addPhotoUrlAndThumbnailForItem(
					userId, itemId,
					thumbnailInputStream, thumbnailMimeType, thumbLength,
					photoUrl);

			index.indexThumbnailSize(itemId, type, photoNo, thumbWidth, thumbHeight);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to store thumbnail", ex);
		} catch (ItemIndexException ex) {
			throw new ItemStorageException("Failed to index thumbnail sizes", ex);
		}
		finally {
			releaseLock(lock);
		}
	}

	@Override
	public void movePhotoAndThumbnailForItem(String userId, String itemId, Class<? extends Item> type, int photoNo, int toIndex)
			throws ItemStorageException {
		
		final Lock lock = obtainLock(userId, itemId);
		
		try {
			xmlStorage.movePhotoAndThumbnailForItem(userId, itemId, photoNo, toIndex);
			index.movePhotoAndThumbnailForItem(itemId, type, photoNo, toIndex);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to move item", ex);
		} catch (ItemIndexException ex) {
			throw new ItemStorageException("Failed to move index thumbnail sizes", ex);
		}
		finally {
			releaseLock(lock);
		}
	}

	@Override
	public void deletePhotoAndThumbnailForItem(String userId, String itemId, Class<? extends Item> type, int photoNo) throws ItemStorageException {

		final Lock lock = obtainLock(userId, itemId);
		
		try {
			xmlStorage.deletePhotoAndThumbnailForItem(userId, itemId, photoNo);
			index.deletePhotoAndThumbnailForItem(itemId, type, photoNo);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to delete photo and thumbnail", ex);
		} catch (ItemIndexException ex) {
			throw new ItemStorageException("Failed to delete index thumbnail sizes", ex);
		}
		finally {
			releaseLock(lock);
		}
	}

	@Override
	public void deleteItem(String userId, String itemId, Class<? extends Item> type) throws ItemStorageException {

		final Lock lock = obtainLock(userId, itemId);

		try {
			index.deleteItem(itemId, type);
		}
		catch (ItemIndexException ex) {
			System.err.println("## failed to delete from index: " + ex);
		}
		try {
			xmlStorage.deleteAllItemFiles(userId, itemId);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to delete item files", ex);
		}
		finally {
			releaseLock(lock);
		}
		
		try {
			lockProvider.deleteLock(userId, itemId);
		} catch (LockException ex) {
			throw new ItemStorageException("Failed to delete lock for " + itemId, ex);
		}
	}

	@Override
	public void close() throws Exception {
		
	}
	
	@Override
	public InputStream retrieveAndConcatenateThumbnails(String[] itemIdStrings) throws ItemStorageException {
		
		// Search in index to get convert to userId/itemId mapping
		final ItemId[] itemIds;
		try {
			itemIds = index.expandToItemIdUserId(itemIdStrings);
		} catch (ItemIndexException ex) {
			throw new ItemStorageException("Failed to expand item IDs from index", ex);
		}
		
		// Retrieve thumbnails across user IDs from storage. Pass in all since might retrieve in parallell
		final Map<String, Integer> map = makeItemIdToIndexMap(itemIds);
		
		final List<Thumbnail> sorted = new ArrayList<>(itemIds.length);
		
		// default to no thumbnail
		for (int i = 0; i < itemIds.length; ++ i) {
			sorted.add(new Thumbnail("", 0, null));
		}
		
		try {
			xmlStorage.retrieveThumbnails(itemIds, (imageResult, itemId) -> {
				if (itemId == null) {
					throw new IllegalArgumentException("itemId == null");
				}

				if (!map.containsKey(itemId.getItemId())) {
					throw new IllegalStateException("No item id " + itemId.getItemId() + " in " + map);
				}
				
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
		
		final Lock lock = obtainLock(userId, itemId);
		
		try {
			return xmlStorage.getNumFiles(userId, itemId, ItemFileType.THUMBNAIL);
		} catch (StorageException ex) {
			throw new ItemStorageException("Failed to get number of files", ex);
		}
		finally {
			releaseLock(lock);
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
