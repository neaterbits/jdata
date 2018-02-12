package com.test.cv.dao.jpa;

import java.util.List;

import com.test.cv.dao.IFoundItem;
import com.test.cv.model.Item;
import com.test.cv.model.ItemAttributeValue;

final class JPAFoundItem implements IFoundItem {

	private final Item item;
	
	JPAFoundItem(Item item) {
		
		if (item == null) {
			throw new IllegalArgumentException("item == null");
		}
		
		this.item = item;
	}

	@Override
	public List<ItemAttributeValue<?>> getAttributes() {
		// Retrieve the attributes by reflection
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Item getItem() {
		return item;
	}
}
