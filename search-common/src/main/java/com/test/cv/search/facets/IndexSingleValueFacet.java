package com.test.cv.search.facets;

import java.util.List;

public class IndexSingleValueFacet {

	private final Object value;
	private final Object displayValue;
	private int matchCount;
	private List<IndexFacetedAttributeResult> subFacets;

	public IndexSingleValueFacet(Object value, Object displayValue, List<IndexFacetedAttributeResult> subFacets) {
		if (value == null) {
			throw new IllegalArgumentException("value == null");
		}

		this.value = value;
		this.displayValue = displayValue;
		this.matchCount = 0;
		this.subFacets = subFacets;
	}

	public Object getValue() {
		return value;
	}
	
	public Object getDisplayValue() {
		return displayValue;
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

	public void setSubFacets(List<IndexFacetedAttributeResult> subFacets) {
		this.subFacets = subFacets;
	}

	@Override
	public String toString() {
		return "IndexSingleValueFacet [value=" + value + ", matchCount=" + matchCount + ", subFacets=" + subFacets
				+ "]";
	}
}
