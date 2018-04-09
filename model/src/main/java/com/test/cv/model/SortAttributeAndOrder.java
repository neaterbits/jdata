package com.test.cv.model;

public class SortAttributeAndOrder {

	private final SortAttribute attribute;
	private final SortOrder sortOrder;
	
	public SortAttributeAndOrder(SortAttribute attribute, SortOrder sortOrder) {
		
		if (attribute == null) {
			throw new IllegalArgumentException("attribute == null");
		}
		
		if (sortOrder == null) {
			throw new IllegalArgumentException("sortOrder == null");
		}
		
		this.attribute = attribute;
		this.sortOrder = sortOrder;
	}

	public SortAttribute getAttribute() {
		return attribute;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}
}
