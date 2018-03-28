package com.test.cv.dao;

/**
 * For creating persisted locks for items,
 * these are necessary eg. when storing/updated in both S3 and ElasticSearch
 * since there is no transactions support in these.
 * This we must create locks elsewhere to assure atomicity.
 * 
 * Obtaining a lock can be costly but is only necessary for item creation and updates,
 * which for a sales portal and similar software is very few, this is write-few, read-many
 * 
 */
public interface LockDAO {

	void createLock(String itemId);
	
	Object lock(String itemId);
	
	void relaseLock(Object lock);
	
	void deleteLock(String itemId);
	
}
