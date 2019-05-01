package com.test.salesportal.search.facets;

import java.util.List;

import com.test.salesportal.model.Item;

public class TypeFacets {

	private final Class<? extends Item> type;
	private final List<IndexFacetedAttributeResult> attributes;

	public TypeFacets(Class<? extends Item> type, List<IndexFacetedAttributeResult> attributes) {
		
		if (type == null) {
			throw new IllegalArgumentException("type == null");
		}

		if (attributes == null) {
			throw new IllegalArgumentException("attributes == null");
		}

		this.type = type;
		this.attributes = attributes;
	}

	public Class<? extends Item> getType() {
		return type;
	}

	public List<IndexFacetedAttributeResult> getAttributes() {
		return attributes;
	}
}
