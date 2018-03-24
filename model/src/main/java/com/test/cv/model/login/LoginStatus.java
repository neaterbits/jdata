package com.test.cv.model.login;

public enum LoginStatus {

	UNKNOWN_PHONENO, 	// Not known phone number, must be manually approved
	APPROVING,			// Waiting for approval
	APPROVED;			// Approved, login code will be sent to user

}
