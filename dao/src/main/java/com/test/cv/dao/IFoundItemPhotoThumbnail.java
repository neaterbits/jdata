package com.test.cv.dao;

import java.util.List;

import com.test.cv.model.ItemPhotoCategory;

public interface IFoundItemPhotoThumbnail {

	String getId();
	
	int getIndex();
	
	int getWidth();
	
	int getHeight();
	
	String getMimeType();
	
	List<ItemPhotoCategory> getCategories();
	
	byte[] getData();
}
