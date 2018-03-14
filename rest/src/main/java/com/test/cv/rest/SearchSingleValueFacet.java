package com.test.cv.rest;

import java.util.Arrays;
import java.util.List;

public class SearchSingleValueFacet {
	
	private Object value;
	
	// Number matching this attribute
	private int matchCount;

	// eg item type is sub attribute of item category (snowboard under sports)
	// or county is sub attribute of state (for apartments and houses)
	private List<SearchFacetedAttributeResult> subAttributes;

	public SearchSingleValueFacet() {
		
	}

	public SearchSingleValueFacet(Object value, int matchCount, SearchFacetedAttributeResult ... subAttributes) {
		if (value == null) {
			throw new IllegalArgumentException("value == null");
		}
		
		this.value = value;
		this.matchCount = matchCount;
		
		this.subAttributes = subAttributes.length == 0 ? null : Arrays.asList(subAttributes);
	}
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(int matchCount) {
		this.matchCount = matchCount;
	}

	public List<SearchFacetedAttributeResult> getSubAttributes() {
		return subAttributes;
	}

	public void setSubAttributes(List<SearchFacetedAttributeResult> subAttributes) {
		this.subAttributes = subAttributes;
	}
}
