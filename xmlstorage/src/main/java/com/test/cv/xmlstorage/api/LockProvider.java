package com.test.cv.xmlstorage.api;

public interface LockProvider {
	public interface Lock {
		
	}
	
	public class LockException extends Exception {

		private static final long serialVersionUID = 1L;

		public LockException(String message, Throwable cause) {
			super(message, cause);
		}

		public LockException(String message) {
			super(message);
		}
	}
	
	/**
	 * Create a lock for later use, eg. a lock file or a row in database
	 * @param userId
	 * @param itemId
	 */
	
	void createLock(String userId, String itemId) throws LockException;
	
	
	/**
	 * Lock files for this user and this item by creating a lock file or object
	 * This might block for a while if there are other ongoing operations for this userId/itemId combination
	 * @param userId
	 * @param itemId
	 */
	Lock obtainLock(String userId, String itemId) throws LockException;
	
	/**
	 * Release lock for userId/itemId
	 * 
	 * @param userId
	 * @param itemId
	 */
	void releaseLock(Lock lock);
	
	/**
	 * Delete a previously created lock
	 * @param userId
	 * @param itemId
	 */
	
	void deleteLock(String userId, String itemId) throws LockException;
	
}
