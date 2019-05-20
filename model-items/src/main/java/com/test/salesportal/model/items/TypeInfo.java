package com.test.salesportal.model.items;

import com.test.salesportal.model.items.attributes.ClassAttributes;
import com.test.salesportal.model.items.base.ItemTypes;

public class TypeInfo {
	private final Class<? extends Item> type;
	private final String facetDisplayName;
	private final ClassAttributes attributes;

	public TypeInfo(Class<? extends Item> type, String facetDisplayName, ClassAttributes attributes) {
		this.type = type;
		this.facetDisplayName = facetDisplayName;
		this.attributes = attributes;
	}

	public Class<? extends Item> getType() {
		return type;
	}
	
	public String getTypeName() {
		return ItemTypes.getTypeName(type);
	}
	
	public String getFacetDisplayName() {
		return facetDisplayName;
	}

	public ClassAttributes getAttributes() {
		return attributes;
	}
}
