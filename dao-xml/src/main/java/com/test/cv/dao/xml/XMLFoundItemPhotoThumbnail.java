package com.test.cv.dao.xml;

import java.util.List;

import com.test.cv.dao.BaseFoundItemPhotoThumbnail;
import com.test.cv.dao.IFoundItemPhotoThumbnail;
import com.test.cv.model.ItemPhotoCategory;

final class XMLFoundItemPhotoThumbnail extends BaseFoundItemPhotoThumbnail implements IFoundItemPhotoThumbnail {

	public XMLFoundItemPhotoThumbnail(String id, String itemId, int index, String mimeType, int width, int height, List<ItemPhotoCategory> categories, byte[] data) {
		super(id, itemId, index, mimeType, width, height, categories, data);
	}
}
