package com.test.salesportal.rest.search.model.facetresult;

import java.math.BigDecimal;

public class SearchFacetedAttributeDecimalRangeResult extends SearchFacetedAttributeRangeResult<BigDecimal> {

	public SearchFacetedAttributeDecimalRangeResult() {

	}

	public SearchFacetedAttributeDecimalRangeResult(BigDecimal lower, BigDecimal upper, int matchCount) {
		super(lower, upper, matchCount);
	}
}
