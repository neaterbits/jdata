package com.test.cv.dao.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;import javax.imageio.stream.ImageOutputStreamImpl;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

import com.test.cv.common.IOUtil;
import com.test.cv.dao.IFoundItem;
import com.test.cv.dao.IFoundItemPhotoThumbnail;
import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.model.Item;
import com.test.cv.model.ItemPhoto;
import com.test.cv.model.ItemPhotoCategory;
import com.test.cv.model.ItemPhotoThumbnail;

public final class JPAItemDAO extends JPABaseDAO implements IItemDAO {

	public JPAItemDAO(String persistenceUnitName) {
		super(persistenceUnitName);
	}
	
	public JPAItemDAO(EntityManagerFactory entityManagerFactory) {
		super(entityManagerFactory);
	}

	@Override
	public IFoundItem getItem(String userId, String itemId) {
		final Item item = entityManager.find(Item.class, Long.parseLong(itemId));
		
		return item == null ? null : new JPAFoundItem(item);
	}

	@Override
	public List<IFoundItemPhotoThumbnail> getPhotoThumbnails(String userId, String itemId) {

		final String query = "select ipt.id, ipt.mimeType, ipc, ipt.data "
					+ " from ItemPhotoThumbnail ipt "
				    + " left outer join ipt.photo ip "
				    + " left outer join ip.categories ipc "
					+ " where ipt.item.id = :itemId";
		
		@SuppressWarnings("unchecked")
		final List<Object[]> found = entityManager.createQuery(query)
			.setParameter("itemId", Long.parseLong(itemId))
			.getResultList();

		final List<IFoundItemPhotoThumbnail> result = new ArrayList<>(found.size());
		
		for (Object [] columns : found) {
			final long id = (Long)columns[0];
			final String mimeType = (String)columns[1];
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final List<ItemPhotoCategory> categories = (List)columns[2];
			
			final byte []data = (byte[])columns[3];
			
			result.add(new JPAFoundItemPhotoThumbnail(id, itemId, mimeType, categories, data));
		}
		
		return result;
	}

	@Override
	public ItemPhoto getItemPhoto(String userId, IFoundItemPhotoThumbnail thumbnail) {
		
		final long thumbnailId = Long.parseLong(thumbnail.getId());

		/*
		final String query = "from ItemPhoto ip where ip.id = "
				+ " (select ipt.photo.id from ItemPhotoThumbnail ipt where ipt.id = :iptId)";
		*/

		final String query = "select ipt.photo from ItemPhotoThumbnail ipt " +
					" where ipt.id = :iptId";

		ItemPhoto photo = null;
		
		try {
			photo = entityManager.createQuery(query, ItemPhoto.class)
				.setParameter("iptId", thumbnailId)
				.getSingleResult();
		}
		catch (NoResultException ex) {
			
		}

		return photo;
	}

	@Override
	public String addItem(String userId, Item item) {

		final EntityTransaction tx = entityManager.getTransaction();
		
		tx.begin();

		try {
			entityManager.persist(item);
			
			tx.commit();
		}
		finally {
			
		}
		
		return String.valueOf(item.getId());
	}

	@Override
	public void addPhotoAndThumbnailForItem(String userId, String itemId, InputStream thumbnailInputStream,
			String thumbnailMimeType, InputStream photoInputStream, String photoMimeType) throws ItemStorageException {

		final EntityTransaction tx = entityManager.getTransaction();
		
		boolean ok = false;
		
		tx.begin();
		
		try {
			final IFoundItem foundItem = getItem(userId, itemId);

			if (foundItem == null) {
				throw new ItemStorageException("No such item");
			}
			
			final Item item = foundItem.getItem();

			final ItemPhotoThumbnail thumbnail = new ItemPhotoThumbnail();

			thumbnail.setMimeType(thumbnailMimeType);
			thumbnail.setItem(item);
			
			try {
				thumbnail.setData(IOUtil.readAll(thumbnailInputStream));
			} catch (IOException ex) {
				throw new ItemStorageException("Failed to read thumbnail data", ex);
			}

			final ItemPhoto photo = new ItemPhoto();

			photo.setMimeType(photoMimeType);
			try {
				photo.setData(IOUtil.readAll(photoInputStream));
			} catch (IOException ex) {
				throw new ItemStorageException("Failed to read photo data", ex);
			}

			entityManager.persist(photo);
			
			thumbnail.setPhoto(photo);
			
			entityManager.persist(thumbnail);

			entityManager.persist(item);
			
			tx.commit();
			
			ok = true;
		}
		finally {
			if (!ok) {
				tx.rollback();
			}
		}
	}

	@Override
	public void deletePhotoAndThumbnailForItem(String userId, String itemId, int photoNo) throws ItemStorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteItem(String userId, String itemId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateItem(String userId, Item item) {
		entityManager.persist(item);
	}
}
