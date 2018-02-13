package com.test.cv.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;


import com.test.cv.dao.IFoundItem;
import com.test.cv.dao.IFoundItemPhotoThumbnail;
import com.test.cv.dao.IItemDAO;
import com.test.cv.model.Item;
import com.test.cv.model.ItemPhoto;
import com.test.cv.model.ItemPhotoCategory;

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

		final String query = "select ipt.id, ipt.mimeType, ipt.photo.categories, ipt.data from ItemPhotoThumbnail ipt where ipt.item = :itemId";
		
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
			
			result.add(new JPAFoundItemPhotoThumbnail(id, mimeType, categories, data));
		}
		
		return result;
	}

	@Override
	public ItemPhoto getItemPhoto(String userId, IFoundItemPhotoThumbnail thumbnail) {
		
		final long thumbnailId = Long.parseLong(thumbnail.getId());
		
		final String query = "from ItemPhoto where id = (select ipt.photo.id from ItemPhotoThumbnail ipt where ipt.id = :iptId)";
		
		return entityManager.createQuery(query, ItemPhoto.class)
				.setParameter("iptId", thumbnailId)
				.getSingleResult();
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
	public void updateItem(String userId, Item item) {
		entityManager.persist(item);
	}
}
