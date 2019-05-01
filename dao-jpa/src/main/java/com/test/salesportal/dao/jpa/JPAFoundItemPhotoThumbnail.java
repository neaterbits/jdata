package com.test.salesportal.dao.jpa;

import java.util.List;

import com.test.salesportal.dao.BaseFoundItemPhotoThumbnail;
import com.test.salesportal.dao.IFoundItemPhotoThumbnail;
import com.test.salesportal.model.ItemPhotoCategory;

final class JPAFoundItemPhotoThumbnail extends BaseFoundItemPhotoThumbnail implements IFoundItemPhotoThumbnail {

	JPAFoundItemPhotoThumbnail(long id, String itemId, int index, String mimeType, int width, int height, List<ItemPhotoCategory> categories, byte[] data) {
		super(String.valueOf(id), itemId, index, mimeType, width, height, categories, data);
	}
}
