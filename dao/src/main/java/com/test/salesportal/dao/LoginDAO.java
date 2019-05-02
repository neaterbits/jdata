package com.test.salesportal.dao;

import java.util.Date;

import com.test.salesportal.model.login.LoginStatus;

/**
 * DAO for adding users for login, for now this just supports adding some
 * basic SMS-code authentication
 */

public interface LoginDAO extends AutoCloseable {
	
	/**
	 * Get login status for user.
	 * If user does not exist, add within same transaction
	 * so that setting can be done in same transaction
	 * @param phoneNo
	 * @param initialStatus
	 * @return
	 */
	LoginStatus getOrAddUser(String phoneNo, LoginStatus initialStatus);
	
	LoginStatus getLoginStatus(String phoneNo);
	
	public default void approveUser(String phoneNo) {
		updateLoginStatus(phoneNo, LoginStatus.APPROVED);
	}
	
	void deleteUser(String phoneNo);
	
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
