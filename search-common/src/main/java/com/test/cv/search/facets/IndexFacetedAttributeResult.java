package com.test.cv.search.facets;

import com.test.cv.model.ItemAttribute;

public abstract class IndexFacetedAttributeResult {

	private final ItemAttribute attribute;

	public IndexFacetedAttributeResult(ItemAttribute attribute) {
	
		if (attribute == null) {
			throw new IllegalArgumentException("attribute == null");
		}

		this.attribute = attribute;
	}

	public ItemAttribute getAttribute() {
		return attribute;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [attribute=" + attribute.getName() + "]";
	}
}
