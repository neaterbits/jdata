package com.test.cv.rest;

import java.util.List;

public class SearchSimpleFacetedAttributeResult extends SearchFacetedAttributeResult {

	// Number matching this attribute
	private int matchCount;

	// eg item type is sub attribute of item category (snowboard under sports)
	// or county is sub attribute of state (for apartments and houses)
	private List<SearchFacetedAttributeResult> subAttributes;

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
