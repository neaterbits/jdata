package com.test.salesportal.dao;

/**
 * General storage related exception
 */

public class CVStorageException extends Exception {

	private static final long serialVersionUID = 1L;

	public CVStorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
