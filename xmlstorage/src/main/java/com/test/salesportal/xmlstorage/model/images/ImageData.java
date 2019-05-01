package com.test.salesportal.xmlstorage.model.images;

import javax.xml.bind.annotation.XmlElement;

public class ImageData {

	private String fileName;
	private String mimeType;
	private int size; // in bytes
	private int width;
	private int height;

	@XmlElement
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@XmlElement
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@XmlElement
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@XmlElement
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@XmlElement
	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}


}
