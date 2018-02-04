package com.test.cv.dao;

import com.test.cv.model.cv.CV;
import com.test.cv.model.cv.Language;

public interface ICVDAO extends AutoCloseable {

	/**
	 * Locate a CV for a particular user, returning it with texts in the languages specified
	 * 
	 * @param userId ID of user
	 * 
	 * @return the CV, or null if none was found
	 */
	CV findCV(String userId, Language ...languages) throws CVStorageException;
}
