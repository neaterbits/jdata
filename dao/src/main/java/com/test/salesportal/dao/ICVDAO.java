package com.test.salesportal.dao;

import com.test.salesportal.model.cv.CV;
import com.test.salesportal.model.cv.Language;

/**
 * For storing structured CVs, like in job portals.
 * Not directly related to salesportal.
 * 
 */

public interface ICVDAO extends AutoCloseable {

	/**
	 * Locate a CV for a particular user, returning it with texts in the languages specified
	 * 
	 * @param userId ID of user
	 * @param langauges in order of preference
	 * 
	 * @return the CV, or null if none was found
	 */
	CV findCV(String userId, Language ...languages) throws CVStorageException;

	
	/**
	 * Locate a CV for a particular user for editing, ie. return texts in all languages so that may be saved back
	 * 
	 * @param userId ID of user
	 * 
	 * @return the CV, or null if none was found
	 */
	CV findCVForEdit(String userId) throws CVStorageException;

	/**
	 * Create CV for user in case it was not stored already
	 */
	
	void createCV(String userId, CV cv) throws CVStorageException;
	
	/**
	 * Update CV, here must pass the whole updated CV as retrieved from findCVForEdit()
	 * 
	 */
	void updateCV(String userId, CV cv) throws CVStorageException;
	
	/**
	 * Delete CV, a user can be registered while not havig a CV stored any longer
	 */
	void deleteCV(String userId) throws CVStorageException;
}
