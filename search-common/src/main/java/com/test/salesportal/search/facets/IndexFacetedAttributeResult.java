package com.test.salesportal.search.facets;

import com.test.salesportal.model.items.ItemAttribute;

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
	
	public abstract boolean hasValueOrRangeMatches();

	public ItemAttribute getAttribute() {
		return attribute;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [attribute=" + attribute.getName() + ", noAttributeValueCount=" + noAttributeValueCount + "]";
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
