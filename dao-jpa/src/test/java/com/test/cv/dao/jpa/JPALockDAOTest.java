package com.test.cv.dao.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import junit.framework.TestCase;

public class JPALockDAOTest extends TestCase {

	public void testPlainLocking() throws Exception {
		
		try (JPALockDAO lockDAO = new JPALockDAO(JPANames.PERSISTENCE_UNIT_DERBY)) {
			
			final String itemId = "12345";
			
			lockDAO.createLock(itemId);
			
			try {
				final Object lock = lockDAO.lock(itemId);
	
				assertThat(lock).isNotNull();

				final boolean isLocked = lockDAO.isLocked(itemId);
				
				assertThat(isLocked).isTrue();
	
				lockDAO.relaseLock(lock);
			}
			finally {
				lockDAO.deleteLock(itemId);
			}
		}
	}

	public void testOverlappingLocking() throws Exception {
		try (JPALockDAO lockDAO = new JPALockDAO(JPANames.PERSISTENCE_UNIT_DERBY)) {
			
			final String itemId = "12345";
			
			lockDAO.createLock(itemId);
			
			try {
				final Object lock = lockDAO.lock(itemId);
	
				assertThat(lock).isNotNull();

				final boolean isLocked = lockDAO.isLocked(itemId);
				
				assertThat(isLocked).isTrue();
	
				// Spawn a separate thread to lock
				
				final long threadStartTime = System.currentTimeMillis();

				final Value<Long> threadLockedTime = new Value<>();
				
				final Thread t = new Thread(() -> {
					final Object threadLock = lockDAO.lock(itemId);
					
					final long lockedTime = System.currentTimeMillis();

					threadLockedTime.set(lockedTime); // Access from outside scope
					
					lockDAO.relaseLock(threadLock);
				});
				
				
				t.start();

				Thread.sleep(5000L);

				lockDAO.relaseLock(lock);
				
				// Thread not joined until completed locking
				t.join();

				// Ought to have waited at least 5000ms since this thread waited that long
				assertThat(threadLockedTime.value).isNotNull();
				
				final long timeSinceLocked = threadLockedTime.value - threadStartTime;
				assertThat(timeSinceLocked).isGreaterThan(5000L);
				
			}
			finally {
				lockDAO.deleteLock(itemId);
			}
		}
	}
	
	private static class Value<T> {
		private T value;
		
		void set(T value) {
			this.value = value;
		}
	}

}
