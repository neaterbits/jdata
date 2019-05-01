package com.test.salesportal.xmlstorage.api;

public enum ItemFileType {
	
	XML("xml"),
	THUMBNAIL("thumbnail"),
	PHOTO("photo");
	
	
	private final String directoryName;

	private ItemFileType(String directoryName) {
		this.directoryName = directoryName;
	}

	public String getDirectoryName() {
		return directoryName;
	}
}
