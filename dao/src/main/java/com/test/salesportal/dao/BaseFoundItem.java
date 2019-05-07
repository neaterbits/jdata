package com.test.salesportal.dao;

import java.util.List;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttributeValue;

public abstract class BaseFoundItem implements IFoundItem {
	private final Item item;
	
	protected BaseFoundItem(Item item) {
		
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
	public Item getItem() {
		return item;
	}

	@Override
	public final String getTitle() {
		return item.getTitle();
	}
}