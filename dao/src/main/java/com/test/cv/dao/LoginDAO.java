package com.test.cv.dao;

import java.util.Date;

import com.test.cv.model.login.LoginStatus;

public interface LoginDAO extends AutoCloseable {
	
	/**
	 * Get login status for user.
	 * If user does not exist, add within same transaction
	 * so that setting can be done in same transaction
	 * @param phoneNo
	 * @return
	 */
	LoginStatus getOrAddUser(String phoneNo);
	
	LoginStatus getLoginStatus(String phoneNo);
	
	void updateLoginStatus(String phoneNo, LoginStatus status);
	
	void storeCode(String phoneNo, String code, Date timeGenerated);
	
	/**
	 * Check if code is set
	 * @param phoneNo
	 * @param code
	 * @return
	 */
	LoginCode getLoginStatusAndCode(String phoneNo);
}
