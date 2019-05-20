package com.test.salesportal.dao;

import java.util.List;

import com.test.salesportal.model.items.ItemAttributeValue;
import com.test.salesportal.model.items.base.TitlePhotoItem;

public abstract class BaseFoundItem implements IFoundItem {
	private final TitlePhotoItem item;
	
	protected BaseFoundItem(TitlePhotoItem item) {
		
		if (item == null) {
			throw new IllegalArgumentException("item == null");
		}
		
		this.item = item;
	}

	@Override
	public List<ItemAttributeValue<?>> getAttributes() {
		// Retrieve the  attributes by reflection
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public TitlePhotoItem getItem() {
		return item;
	}

	@Override
	public final String getTitle() {
		return item.getTitle();
	}
}
