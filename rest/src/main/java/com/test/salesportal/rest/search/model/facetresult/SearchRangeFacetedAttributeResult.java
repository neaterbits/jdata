package com.test.salesportal.rest.search.model.facetresult;

import java.util.List;

public class SearchRangeFacetedAttributeResult extends SearchFacetedAttributeResult {
	
	// A list of ranges with matchcount for each
	private List<SearchFacetedAttributeRangeResult<?>> ranges;

	public SearchRangeFacetedAttributeResult() {

	}

	public SearchRangeFacetedAttributeResult(String id, String name, Integer noAttributeValueCount) {
		super(id, name, noAttributeValueCount);
	}

	public List<SearchFacetedAttributeRangeResult<?>> getRanges() {
		return ranges;
	}

	public void setRanges(List<SearchFacetedAttributeRangeResult<?>> ranges) {
		this.ranges = ranges;
	}
}
