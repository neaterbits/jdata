package com.test.cv.dao.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.test.cv.dao.IFoundItem;
import com.test.cv.dao.IFoundItemPhotoThumbnail;
import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.model.Item;
import com.test.cv.model.ItemPhoto;
import com.test.cv.xmlstorage.api.IItemStorage;
import com.test.cv.xmlstorage.api.StorageException;

public class XMLItemDAO extends XMLBaseDAO implements IItemDAO {

	private static final JAXBContext jaxbContext;

	static {
		try {
			jaxbContext = JAXBContext.newInstance(Item.class);
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

			found = new XMLFoundItem(item);
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
	public List<IFoundItemPhotoThumbnail> getPhotoThumbnails(String userId, String itemId) {

		// Thumbnails are stored as separate files
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemPhoto getItemPhoto(String userId, IFoundItemPhotoThumbnail thumbnail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String addItem(String userId, Item item) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void updateItem(String userId, Item item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws Exception {
		
	}
}
