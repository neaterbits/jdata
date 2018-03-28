package com.test.cv.dao.jpa;

import javax.persistence.EntityManagerFactory;

import com.test.cv.dao.LockDAO;
import com.test.cv.model.lock.ItemLock;

public class JPALockDAO extends JPABaseDAO implements LockDAO {
	
	private final long lockRetryMillis;
	
	public JPALockDAO(String persistenceUnitName) {
		this(persistenceUnitName, 500L);
	}

	public JPALockDAO(String persistenceUnitName, long lockRetryMillis) {
		super(persistenceUnitName);
		
		this.lockRetryMillis = lockRetryMillis;
	}

	@Override
	public void createLock(String itemId) {
		if (itemId == null) {
			throw new IllegalArgumentException("itemId == null");
		}

		performInTransaction(() -> {
			// Run an insert directly to detect entry already existing atomically,
			// must use native query for this, sicne no JPA insert
			// and entityManager.persist() might update as well
			// while a native SQL insert will return error if exists
			final int inserted = entityManager.createNativeQuery(
					"insert into item_lock (item_id, locked) values (?, 0)")
					
				.setParameter(1, itemId)
				.executeUpdate();
			
			if (inserted != 1) {
				throw new IllegalStateException("Should have inserted item: " + inserted);
			}

			return null;
		});
	}

	@Override
	public void deleteLock(String itemId) {
		if (itemId == null) {
			throw new IllegalArgumentException("itemId == null");
		}

		performInTransaction(() -> {
			final int deleted = entityManager.createQuery("delete from ItemLock l where l.itemId=:itemId")
				.setParameter("itemId", itemId)
				.executeUpdate();
			
			if (deleted != 1) {
				throw new IllegalStateException("Not able to delete lock row: " + deleted);
			}
			
			return null;
		});
	}

	@Override
	public Object lock(String itemId) {
		if (itemId == null) {
			throw new IllegalArgumentException("itemId == null");
		}

		final int timeoutMillis = 10 * 1000;
		
		// Must run in a loop with a timeout since there is no way to
		// wait for lock to be freed in DB call itself
		final long beforeLock = System.currentTimeMillis();
		
		for (;;) {
		
			// The atomic part
			final int updated = performInTransaction(() -> {
				return entityManager.createQuery("update ItemLock l set l.locked = true where l.itemId=:itemId and l.locked = false")
						.setParameter("itemId", itemId)
						.executeUpdate();
			});
			
			if (updated == 0) {
				// Wait a bit and retry
				try {
					Thread.sleep(lockRetryMillis);
				} catch (InterruptedException ex) {
					throw new IllegalStateException("Interrupted while waiting for lock");
				}
				
				final long now = System.currentTimeMillis();
				final long diff = now - beforeLock;
				
				if (diff > timeoutMillis) {
					throw new IllegalStateException("Lock timed out for itemid " + itemId + ": " + diff);
				}
			}
			else if (updated == 1) {
				// Got lock now
				break;
			}
			else {
				throw new IllegalStateException("More than one row updated? " + updated);
			}
		}
		
		return itemId;
	}

	@Override
	public void relaseLock(Object lock) {
		if (lock == null) {
			throw new IllegalArgumentException("lock == null");
		}
		
		final int updated = performInTransaction(() -> {
			return entityManager.createQuery("update ItemLock l set l.locked = false where l.itemId = :itemId")
				.setParameter("itemId", lock)
				.executeUpdate();
		});
		
		if (updated == 0) {
			throw new IllegalStateException("No entries updated for lock");
		}
	}
	
	// For white-box testing
	boolean isLocked(Object lock) {
		if (lock == null) {
			throw new IllegalArgumentException("lock == null");
		}

		final ItemLock l = entityManager.find(ItemLock.class, lock);
		
		return l.isLocked();
	}
}
