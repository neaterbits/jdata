package com.test.salesportal.dao;

import java.util.List;

import com.test.salesportal.model.items.photo.ItemPhotoCategory;

public class BaseFoundItemPhotoThumbnail implements IFoundItemPhotoThumbnail {

	private final String id;
	private final String itemId;
	private final int index;
	private final String mimeType;
	private final int width;
	private final int height;
	private final List<ItemPhotoCategory> categories;
	private final byte [] data;

	protected BaseFoundItemPhotoThumbnail(String id, String itemId, int index, String mimeType, int width, int height, List<ItemPhotoCategory> categories, byte[] data) {
		this.id = id;
		this.itemId = itemId;
		this.index = index;
		this.mimeType = mimeType;
		this.width = width;
		this.height = height;
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
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
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
