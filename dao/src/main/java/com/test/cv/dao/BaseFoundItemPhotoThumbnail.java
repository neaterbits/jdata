package com.test.cv.dao;

import java.util.List;

import com.test.cv.model.ItemPhotoCategory;

public class BaseFoundItemPhotoThumbnail implements IFoundItemPhotoThumbnail {

	private final String id;
	private final String itemId;
	private final int index;
	private final String mimeType;
	private final List<ItemPhotoCategory> categories;
	private final byte [] data;

	protected BaseFoundItemPhotoThumbnail(String id, String itemId, int index, String mimeType, List<ItemPhotoCategory> categories, byte[] data) {
		this.id = id;
		this.itemId = itemId;
		this.index = index;
		this.mimeType = mimeType;
		this.categories = categories;
		this.data = data;
	}

	@Override	
	public String getId() {
		return String.valueOf(id);
	}

	public String getItemId() {
		return itemId;
	}

	@Override
	public int getIndex() {
		return index;
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