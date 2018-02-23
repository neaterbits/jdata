package com.test.cv.model.items;

import com.test.cv.model.Item;
import com.test.cv.model.attributes.ClassAttributes;

public class TypeInfo {
	private final Class<? extends Item> type;
	private final ClassAttributes attributes;

	TypeInfo(Class<? extends Item> type, ClassAttributes attributes) {
		this.type = type;
		this.attributes = attributes;
	}

	public Class<? extends Item> getType() {
		return type;
	}

	public ClassAttributes getAttributes() {
		return attributes;
	}
}
