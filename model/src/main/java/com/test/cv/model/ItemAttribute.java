package com.test.cv.model;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;

import com.test.cv.model.attributes.AttributeType;

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

	// TODO use annotations
	public String getDisplayName() {
		return property.getName();
	}
	
	public AttributeType getAttributeType() {
		final Class<?> propertyType = property.getPropertyType();
		
		final AttributeType attributeType;
		
		if (propertyType.equals(String.class)) {
			attributeType = AttributeType.STRING;
		}
		else if (propertyType.equals(Integer.class) || propertyType.equals(int.class)) {
			attributeType = AttributeType.INTEGER;
		}
		else if (propertyType.equals(BigDecimal.class)) {
			attributeType = AttributeType.DECIMAL;
		}
		else {
			throw new IllegalStateException("Unknown property type " + propertyType + " of attribute " + getName() + " of " + itemType.getSimpleName());
		}

		return attributeType;
	}
}
