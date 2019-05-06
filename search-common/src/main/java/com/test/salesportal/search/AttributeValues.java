package com.test.salesportal.search;

import java.util.Map;

import com.test.salesportal.model.PropertyAttribute;

public final class AttributeValues<T extends PropertyAttribute> implements FieldValues<T> {
	private final Map<T, Object> values;

	public AttributeValues(Map<T, Object> values) {
		
		if (values == null) {
			throw new IllegalArgumentException("values == null");
		}

		this.values = values;
	}

	@Override
	public Object getValue(PropertyAttribute attribute) {
		return values.get(attribute);
	}
}
