package com.test.cv.search.facets;

import java.util.List;

import com.test.cv.model.ItemAttribute;

public final class AttributeFacet {

	private final ItemAttribute attribute;
	private final int matchCount;
	private final List<AttributeFacet> subFacets;

	public AttributeFacet(ItemAttribute attribute, int matchCount, List<AttributeFacet> subFacets) {
	
		if (attribute == null) {
			throw new IllegalArgumentException("attribute == null");
		}

		this.attribute = attribute;
		this.matchCount = matchCount;
		this.subFacets = subFacets;
	}

	public ItemAttribute getAttribute() {
		return attribute;
	}

	public int getMatchCount() {
		return matchCount;
	}

	public List<AttributeFacet> getSubFacets() {
		return subFacets;
	}
}
