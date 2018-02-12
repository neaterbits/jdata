package com.test.cv.dao;

import java.util.List;

import com.test.cv.model.ItemPhotoCategory;

public interface IFoundItemPhotoThumbnail {

	long getId();
	
	List<ItemPhotoCategory> getCategories();
	
	byte[] getData();
}
