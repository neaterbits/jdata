package com.test.cv.search.facets;

import java.util.List;

public class IndexSingleValueFacet {

	private final Object value;
	private int matchCount;
	private final List<IndexFacetedAttributeResult> subFacets;

	public IndexSingleValueFacet(Object value, List<IndexFacetedAttributeResult> subFacets) {
		if (value == null) {
			throw new IllegalArgumentException("value == null");
		}

		this.value = value;
		this.matchCount = 0;
		this.subFacets = subFacets;
	}

	public Object getValue() {
		return value;
	}

	public int getMatchCount() {
		return matchCount;
	}
	
	public void increaseMatchCount() {
		++ matchCount;
	}

	public List<IndexFacetedAttributeResult> getSubFacets() {
		return subFacets;
	}

	@Override
	public String toString() {
		return "IndexSingleValueFacet [value=" + value + ", matchCount=" + matchCount + ", subFacets=" + subFacets
				+ "]";
	}
}
