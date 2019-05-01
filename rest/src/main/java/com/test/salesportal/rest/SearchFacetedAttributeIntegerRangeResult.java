package com.test.salesportal.rest;

public class SearchFacetedAttributeIntegerRangeResult extends SearchFacetedAttributeRangeResult<Integer> {

	public SearchFacetedAttributeIntegerRangeResult() {
		super();
	}

	public SearchFacetedAttributeIntegerRangeResult(Integer lower, Integer upper, int matchCount) {
		super(lower, upper, matchCount);
	}
}
