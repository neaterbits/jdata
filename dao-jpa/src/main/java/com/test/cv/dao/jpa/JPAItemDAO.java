package com.test.cv.dao.jpa;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

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

		final String query = "select ipt.id, ipt.index, ipt.mimeType, ipc, ipt.data "
					+ " from ItemPhotoThumbnail ipt "
				    + " left outer join ipt.photo ip "
				    + " left outer join ip.categories ipc "
					+ " where ipt.item.id = :itemId"
					+ " order by ipt.index asc";
		
		@SuppressWarnings("unchecked")
		final List<Object[]> found = entityManager.createQuery(query)
			.setParameter("itemId", Long.parseLong(itemId))
			.getResultList();

		final List<IFoundItemPhotoThumbnail> result = new ArrayList<>(found.size());
		
		for (Object [] columns : found) {
			final long id = (Long)columns[0];
			final int index = (Integer)columns[1];
			final String mimeType = (String)columns[2];
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final List<ItemPhotoCategory> categories = (List)columns[3];
			
			final byte []data = (byte[])columns[4];
			
			result.add(new JPAFoundItemPhotoThumbnail(id, itemId, index, mimeType, categories, data));
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

	private void lockItem(Item item) {
		// TODO is this thread-safe? Some other thread might add so max is changed?
		// lock item crashes, use other type of locking for max-index?
		// entityManager.lock(item, LockModeType.PESSIMISTIC_WRITE);
		
	}
	
	@Override
	public void addPhotoAndThumbnailForItem(String userId, String itemId, InputStream thumbnailInputStream,
			String thumbnailMimeType, int thumbWidth, int thumbHeight, InputStream photoInputStream, String photoMimeType) throws ItemStorageException {

		final EntityTransaction tx = entityManager.getTransaction();
		
		boolean ok = false;
		
		tx.begin();

		final int thumbnailIndex;
		
		try {
			final IFoundItem foundItem = getItem(userId, itemId);

			if (foundItem == null) {
				throw new ItemStorageException("No such item");
			}
			
			final Item item = foundItem.getItem();

			lockItem(item);
			
			final int iptCount = getNumThumbnails(userId, itemId);
			
			if (iptCount == 0) {
				thumbnailIndex = 0;
			}
			else {
				final int maxIndex = entityManager.createQuery("select max(ipt.index) from ItemPhotoThumbnail ipt where ipt.item.id = :itemId", Integer.class)
						.setParameter("itemId", item.getId())
						.getSingleResult();
			
				thumbnailIndex = maxIndex + 1;
			}
			
			final ItemPhotoThumbnail thumbnail = new ItemPhotoThumbnail();

			thumbnail.setMimeType(thumbnailMimeType);
			thumbnail.setItem(item);
			thumbnail.setIndex(thumbnailIndex);
			thumbnail.setWidth(thumbWidth);
			thumbnail.setHeight(thumbHeight);

			try {
				thumbnail.setData(IOUtil.readAll(thumbnailInputStream));
			} catch (IOException ex) {
				throw new ItemStorageException("Failed to read thumbnail data", ex);
			}

			final ItemPhoto photo = new ItemPhoto();

			photo.setItem(item);
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
			if (!ok && tx.isActive()) {
				tx.rollback();
			}
		}
	}
	

	@Override
	public void movePhotoAndThumbnailForItem(String userId, String itemId, int photoNo, int toIndex)
			throws ItemStorageException {
		final EntityTransaction tx = entityManager.getTransaction();

		if (photoNo == toIndex) {
			throw new IllegalArgumentException("Moving to same pos");
		}
		
		boolean ok = false;
		
		tx.begin();
		
		try {
			final IFoundItem foundItem = getItem(userId, itemId);

			if (foundItem == null) {
				throw new ItemStorageException("No such item");
			}
			
			final Item item = foundItem.getItem();

			lockItem(item);
			
			final int iptCount = getNumThumbnails(userId, itemId);
			
			if (toIndex < 0 || toIndex >= iptCount) {
				throw new IllegalArgumentException("toIndex out of range");
			}
			
			final ItemPhotoThumbnail ipt = entityManager.createQuery(
					"from ItemPhotoThumbnail ipt " 
					+ " where ipt.item.id = :itemId "
					+ "   and ipt.index = :photoNo", ItemPhotoThumbnail.class)
					.setParameter("itemId", Long.parseLong(itemId))
					.setParameter("photoNo", photoNo)
					.getSingleResult();

			// Avoid unique constraint error on item_id/index
			// by temporarily setting to Integer.MAX_VALUE
			entityManager.createQuery("update ItemPhotoThumbnail ipt "
					+ " set ipt.index = :toIndex "
					+ " where ipt.id = :id")
			.setParameter("toIndex", Integer.MAX_VALUE)
			.setParameter("id", ipt.getId())
			.executeUpdate();
		

			
			if (toIndex < photoNo) {
				// Moving up in list, move all from toIndex down
				entityManager.createQuery("update ItemPhotoThumbnail ipt "
							+ " set ipt.index = ipt.index + 1 "
							+ " where ipt.item.id = :itemId "
							+ " and ipt.index >= :toIndex"
							+ " and ipt.index < :photoNo"
							)
				.setParameter("itemId", Long.parseLong(itemId))
				.setParameter("toIndex", toIndex)
				.setParameter("photoNo", photoNo)
				.executeUpdate();
			}
			else if (toIndex > photoNo) {
				// Moving down in list, move all upto toIndex one step up
				entityManager.createQuery("update ItemPhotoThumbnail ipt "
						+ " set ipt.index = ipt.index - 1 "
						+ " where ipt.item.id = :itemId "
						+ " and ipt.index <= :toIndex"
						+ " and ipt.index > :photoNo"
						)
			.setParameter("itemId", Long.parseLong(itemId))
			.setParameter("toIndex", toIndex)
			.setParameter("photoNo", photoNo)
			.executeUpdate();
			}
			else {
				throw new IllegalStateException();
			}

			// Update index of item in question via id since other entry now also has this index
			
			// does not always update for some reason
			//ipt.setIndex(toIndex);
			//entityManager.persist(ipt);
			
			// .. so just use update query
			entityManager.createQuery("update ItemPhotoThumbnail ipt "
						+ " set ipt.index = :toIndex "
						+ " where ipt.id = :id")
				.setParameter("toIndex", toIndex)
				.setParameter("id", ipt.getId())
				.executeUpdate();
			
			tx.commit();
			
			ok = true;
		}
		finally {
			if (!ok && tx.isActive()) {
				tx.rollback();
			}
		}
	}

	@Override
	public void deletePhotoAndThumbnailForItem(String userId, String itemId, int photoNo) throws ItemStorageException {

		// Must move all indices if this is not the last one
		
		final Item item = getItem(userId, itemId).getItem();
		
		final EntityTransaction tx = entityManager.getTransaction();
		
		boolean ok = false;
		
		tx.begin();
		
		try {
			lockItem(item);
		
			final int num = getNumThumbnails(userId, itemId);

			/* Does not work because of foreign key constraint
			 * and cascade delete does not work on delete from query
			entityManager.createQuery("delete from ItemPhoto ip "
					+ " where ip.id = (select ipt.photo.id from ItemPhotoThumbnail ipt "
							+ " where ipt.item.id = :itemId "
							+ "   and ipt.index = :photoNo )")
						.setParameter("itemId", Long.parseLong(itemId))
						.setParameter("photoNo", photoNo)
						.executeUpdate();

			entityManager.createQuery("delete from ItemPhotoThumbnail ipt "
									+ " where ipt.item.id = :itemId "
									+ "   and ipt.index = :photoNo")
				.setParameter("itemId", Long.parseLong(itemId))
				.setParameter("photoNo", photoNo)
				.executeUpdate();
			*/
			
			final ItemPhotoThumbnail ipt = entityManager.createQuery("from ItemPhotoThumbnail ipt "
									+ " where ipt.item.id = :itemId "
									+ "   and ipt.index = :photoNo", ItemPhotoThumbnail.class)
				.setParameter("itemId", Long.parseLong(itemId))
				.setParameter("photoNo", photoNo)
				.getSingleResult();
			
			entityManager.remove(ipt);

			if (photoNo < num - 1) {
				// Was not the last index so must decrement by one all with higher index
				entityManager.createQuery("update ItemPhotoThumbnail ipt "
							+ " set ipt.index = ipt.index - 1 "
							+ " where ipt.item.id = :itemId "
							+ " and ipt.index > :photoNo")
				.setParameter("itemId", Long.parseLong(itemId))
				.setParameter("photoNo", photoNo)
				.executeUpdate();
			}
			
			
			tx.commit();
			
			ok = true;
		}
		finally {
			if (!ok && tx.isActive()) {
				tx.rollback();
			}
		}
		
	}

	@Override
	public void deleteItem(String userId, String itemId) {
		final Item item = getItem(userId, itemId).getItem();
		
		final EntityTransaction tx = entityManager.getTransaction();
		
		boolean ok = false;
		
		tx.begin();
		
		try {
			lockItem(item);
			
			// Cascade should remove thumbnails and photos
			// TODO causes constraint error, delete manually
			// entityManager.remove(item);
			
			final List<ItemPhotoThumbnail> thumbnails = entityManager.createQuery("from ItemPhotoThumbnail ipt where ipt.item.id = :itemId", ItemPhotoThumbnail.class)
				.setParameter("itemId", Long.parseLong(itemId))
				.getResultList();
			
			for (ItemPhotoThumbnail thumbnail : thumbnails) {
				// Cascades to photos
				entityManager.remove(thumbnail);
			}
			
			entityManager.remove(item);
			
			tx.commit();
			
			ok = true;
		}
		finally {
			if (!ok && tx.isActive()) {
				tx.rollback();
			}
		}
	}

	@Override
	public void updateItem(String userId, Item item) {
		entityManager.persist(item);
	}

	private static class JPAThumbnail extends Thumbnail {
		private final String itemId;

		public JPAThumbnail(String mimeType, int thumbnailSize, InputStream thumbnail, String itemId) {
			super(mimeType, thumbnailSize, thumbnail);

			this.itemId = itemId;
		}
	}
	@Override
	public InputStream retrieveAndConcatenateThumbnails(String[] itemIds) {
		
		// Query the first thumbnail of every item, using left outer join
		/*
		final String query = "select item.id, ipt.mimeType, ipt.data "
				+ " from Item item, ItemPhotoThumbnail ipt "
				+ " where ipt. = ( select min(ipts.index) from ItemPhotoThumbnail ipts"
				+ "  					where ipts.id = item.id  )"
				+ " and item.id in :itemIds";
		*/
		final String query = "select ipt.item.id, ipt.mimeType, ipt.data "
				+ " from ItemPhotoThumbnail ipt "
				+ " where ipt.item.id in :itemIds"
				+ "  and ipt.index = 0";
		
		final List<Long> ids = Arrays.stream(itemIds)
				.map(itemId -> Long.parseLong(itemId))
				.collect(Collectors.toList());
		
		@SuppressWarnings("unchecked")
		final List<Object[]> rows = (List<Object[]>)entityManager.createQuery(query)
				.setParameter("itemIds", ids)
				.getResultList();

		// We get a result back that is not sorted by item ID so we must sort that here
		final Map<String, Integer> map = new HashMap<>(itemIds.length);

		for (int i = 0; i < itemIds.length; ++ i) {
			map.put(itemIds[i], i);
		}
		
		final List<JPAThumbnail> thumbnails = rows.stream()
			.map(row -> new JPAThumbnail((String)row[1], -1, new ByteArrayInputStream((byte[])row[2]), String.valueOf((Long)row[0])))
			.collect(Collectors.toList());
		
		// Sort the thumbnails according to their place
		final List<JPAThumbnail> sorted = new ArrayList<>(thumbnails.size());
		
		for (int i = 0; i < itemIds.length; ++ i) {
			// Set to empty thumbnail by default
			sorted.add(new JPAThumbnail("", 0, null, itemIds[i]));
		}
		
		for (JPAThumbnail thumbnail : thumbnails) {
			final int index = map.get(thumbnail.itemId);
			
			sorted.set(index, thumbnail);
		}
		
		final Iterator<JPAThumbnail> sortedIterator = sorted.iterator();

		final InputStream stream = new RetrieveThumbnailsInputStream() {

			@Override
			protected Thumbnail getNext() {
				return sortedIterator.hasNext() ? sortedIterator.next() : null;
			}
		};
		
		return stream;
	}

	@Override
	public int getNumThumbnails(String userId, String itemId) {
		return entityManager.createQuery("select count(ipt) from ItemPhotoThumbnail ipt where ipt.item.id = :itemId", Long.class)
				.setParameter("itemId", Long.parseLong(itemId))
				.getSingleResult()
				.intValue();
	}

	@Override
	public int getNumPhotos(String userId, String itemId) {
		return entityManager.createQuery("select count(ip) from ItemPhoto ip where ip.item.id = :itemId", Long.class)
				.setParameter("itemId", Long.parseLong(itemId))
				.getSingleResult()
				.intValue();
	}
}
