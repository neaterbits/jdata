package com.test.salesportal.xmlstorage.api;

public class ImageMetaData {
	public final String mimeType;
	public final int imageSize;
	public final int width;
	public final int height;
	
	public ImageMetaData(String mimeType, int imageSize, int width, int height) {
		
		if (mimeType == null) {
			throw new IllegalArgumentException("mimeType == null");
		}
		
		this.mimeType = mimeType;
		this.imageSize = imageSize;
		this.width = width;
		this.height = height;
	}
}
