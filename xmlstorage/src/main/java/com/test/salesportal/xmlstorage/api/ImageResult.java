package com.test.salesportal.xmlstorage.api;

import java.io.InputStream;

public class ImageResult extends ImageMetaData {

	public final InputStream inputStream;
	
	public ImageResult(String mimeType, int imageSize, InputStream inputStream) {
		super(mimeType, imageSize, -1, -1);
		
		if (inputStream == null) {
			throw new IllegalArgumentException("inputStream == null");
		}
		
		this.inputStream = inputStream;
	}
}
