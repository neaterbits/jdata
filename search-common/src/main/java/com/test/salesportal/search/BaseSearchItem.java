package com.test.salesportal.search;

import com.test.salesportal.model.ItemAttribute;

public abstract class BaseSearchItem implements SearchItem {

	private final String id;
	private final String title;
	private final Integer thumbWidth;
	private final Integer thumbHeight;
	private final FieldValues attributeValues;

	
	protected BaseSearchItem(String id, String title, Integer thumbWidth, Integer thumbHeight, FieldValues attributeValues) {
		this.id = id;
		this.title = title;
		this.thumbWidth = thumbWidth;
		this.thumbHeight = thumbHeight;
		this.attributeValues = attributeValues;
	}

	@Override
	public final String getItemId() {
		return id;
	}

	@Override
	public final String getTitle() {
		return title;
	}

	@Override
	public final Integer getThumbWidth() {
		return thumbWidth;
	}

	@Override
	public final Integer getThumbHeight() {
		return thumbHeight;
	}

	@Override
	public final Object getAttributeValue(ItemAttribute attribute) {
		
		if (attribute == null) {
			throw new IllegalArgumentException("attribute == null");
		}

		return attributeValues.getValue(attribute);
	}
}
