package com.test.cv.model.login;

public enum CodeStatus {
	VERIFIED, 	// verified OK
	EXPIRED, 	// code expired
	NONMATCHING; // Does not match stored code
}
