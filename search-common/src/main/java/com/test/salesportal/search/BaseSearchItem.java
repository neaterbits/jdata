package com.test.salesportal.search;

import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttribute;
import com.test.salesportal.model.items.base.ItemTypes;

public abstract class BaseSearchItem implements SearchItem {

	private final String id;
	private final String title;
	private final Integer thumbWidth;
	private final Integer thumbHeight;
	private final FieldValues<SortAttribute> sortValues;
	private final FieldValues<ItemAttribute> attributeValues;

	
	protected BaseSearchItem(
			String id,
			String title,
			Integer thumbWidth, Integer thumbHeight,
			FieldValues<SortAttribute> sortValues,
			FieldValues<ItemAttribute> attributeValues) {
		this.id = id;
		this.title = title;
		this.thumbWidth = thumbWidth;
		this.thumbHeight = thumbHeight;
		this.sortValues = sortValues;
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
	public Object getSortAttributeValue(SortAttribute attribute, ItemTypes itemTypes) {
		
		if (attribute == null) {
			throw new IllegalArgumentException("attribute == null");
		}

		return sortValues.getValue(attribute);
	}

	@Override
	public final Object getFieldAttributeValue(ItemAttribute attribute) {
		
		if (attribute == null) {
			throw new IllegalArgumentException("attribute == null");
		}

		return attributeValues.getValue(attribute);
	}
}
