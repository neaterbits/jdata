package com.test.cv.dao.jpa;

import java.util.List;

import com.test.cv.dao.BaseFoundItemPhotoThumbnail;
import com.test.cv.dao.IFoundItemPhotoThumbnail;
import com.test.cv.model.ItemPhotoCategory;

final class JPAFoundItemPhotoThumbnail extends BaseFoundItemPhotoThumbnail implements IFoundItemPhotoThumbnail {

	JPAFoundItemPhotoThumbnail(long id, String itemId, int index, String mimeType, int width, int height, List<ItemPhotoCategory> categories, byte[] data) {
		super(String.valueOf(id), itemId, index, mimeType, width, height, categories, data);
	}
}
