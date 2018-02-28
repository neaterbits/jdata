package com.test.cv.rest;

import java.util.List;

public class SearchRangeFacetedAttributeResult extends SearchFacetedAttributeResult {
	
	// A list of ranges with matchcount for each
	private List<SearchFacetedAttributeRangeResult<?>> ranges;

	public List<SearchFacetedAttributeRangeResult<?>> getRanges() {
		return ranges;
	}

	public void setRanges(List<SearchFacetedAttributeRangeResult<?>> ranges) {
		this.ranges = ranges;
	}
}
