package com.test.cv.model;

import java.beans.PropertyDescriptor;

// A searchable attribute for an item and accessor methods
public final class ItemAttribute {

	private final Class<? extends Item> itemType;
	private final PropertyDescriptor property;

	public ItemAttribute(Class<? extends Item> itemType, PropertyDescriptor property) {
	
		if (itemType == null) {
			throw new IllegalArgumentException("itemType == null");
		}
		
		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}

		this.itemType = itemType;
		this.property = property;
	}

	public Class<? extends Item> getItemType() {
		return itemType;
	}
	
	public String getName() {
		return property.getName();
	}
}
