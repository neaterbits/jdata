package com.test.cv.dao;

import com.test.cv.model.CV;
import com.test.cv.model.Language;

public interface ICVDAO extends AutoCloseable {

	/**
	 * Locate a CV for a particular user
	 * 
	 * @param userId ID of user
	 * 
	 * @return the CV, or null if none was found
	 */
	CV findCV(String userId, Language ...languages);
}
