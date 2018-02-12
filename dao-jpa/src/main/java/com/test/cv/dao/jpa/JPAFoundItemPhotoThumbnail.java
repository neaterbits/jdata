package com.test.cv.dao.jpa;

import java.util.List;

import com.test.cv.dao.IFoundItemPhotoThumbnail;
import com.test.cv.model.ItemPhotoCategory;

final class JPAFoundItemPhotoThumbnail implements IFoundItemPhotoThumbnail {

	private final long id;
	private final String mimeType;
	private final List<ItemPhotoCategory> categories;
	private final byte [] data;
	
	JPAFoundItemPhotoThumbnail(long id, String mimeType, List<ItemPhotoCategory> categories, byte[] data) {
		this.id = id;
		this.mimeType = mimeType;
		this.categories = categories;
		this.data = data;
	}

	@Override	
	public String getId() {
		return String.valueOf(id);
	}

	@Override	
	public String getMimeType() {
		return mimeType;
	}

	@Override	
	public List<ItemPhotoCategory> getCategories() {
		return categories;
	}

	@Override	
	public byte[] getData() {
		return data;
	}
}
