package com.test.salesportal.dao.xml;

import java.util.List;

import com.test.salesportal.dao.BaseFoundItemPhotoThumbnail;
import com.test.salesportal.dao.IFoundItemPhotoThumbnail;
import com.test.salesportal.model.ItemPhotoCategory;

final class XMLFoundItemPhotoThumbnail extends BaseFoundItemPhotoThumbnail implements IFoundItemPhotoThumbnail {

	public XMLFoundItemPhotoThumbnail(String id, String itemId, int index, String mimeType, int width, int height, List<ItemPhotoCategory> categories, byte[] data) {
		super(id, itemId, index, mimeType, width, height, categories, data);
	}
}
