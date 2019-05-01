package com.test.salesportal.dao;

public class ItemStorageException extends Exception {

	private static final long serialVersionUID = 1L;

	public ItemStorageException(String message) {
		super(message);
	}

	public ItemStorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
