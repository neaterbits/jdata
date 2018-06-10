package com.test.cv.search;

import java.util.Map;

import com.test.cv.model.ItemAttribute;

public final class AttributeValues implements FieldValues {
	private final Map<ItemAttribute, Object> values;

	public AttributeValues(Map<ItemAttribute, Object> values) {
		
		if (values == null) {
			throw new IllegalArgumentException("values == null");
		}

		this.values = values;
	}

	@Override
	public Object getValue(ItemAttribute attribute) {
		return values.get(attribute);
	}
}