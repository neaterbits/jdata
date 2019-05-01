package com.test.salesportal.model;

public abstract class ItemAttributeValue<T> {

	private final ItemAttribute attribute;
	private final T value;
	
	public ItemAttributeValue(ItemAttribute attribute, T value) {

		if (attribute == null) {
			throw new IllegalArgumentException("attribute == null");
		}
		
		if (value == null) {
			throw new IllegalArgumentException("value == null");
		}
		
		this.attribute = attribute;
		this.value = value;
	}

	public final ItemAttribute getAttribute() {
		return attribute;
	}

	public final T getValue() {
		return value;
	}
}
