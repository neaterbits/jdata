package com.test.cv.search.facets;

import com.test.cv.model.ItemAttribute;

public abstract class IndexFacetedAttributeResult {

	private final ItemAttribute attribute;

	// Count for elements where there were no value
	private int noAttributeValueCount;

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

	public void addToNoAttributeValueCount() {
		++ this.noAttributeValueCount;
	}
	
	public void addToNoAttributeValueCount(int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("count <= 0: " + count);
		}

		this.noAttributeValueCount += count;
	}
	
	public int getNoAttributeValueCount() {
		return noAttributeValueCount;
	}
}
