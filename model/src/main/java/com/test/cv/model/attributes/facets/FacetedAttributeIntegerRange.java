package com.test.cv.model.attributes.facets;

public final class FacetedAttributeIntegerRange {

	private final Integer lower;
	private final Integer upper;
	
	public FacetedAttributeIntegerRange(Integer lower, Integer upper) {
		super();
		this.lower = lower;
		this.upper = upper;
	}

	public Integer getLower() {
		return lower;
	}

	public Integer getUpper() {
		return upper;
	}
}
