package com.test.salesportal.rest.search.model.facetresult;

import com.test.salesportal.model.FacetFiltering;

public class SearchInputFacetedAttributeResult extends SearchFacetedAttributeResult {

	public SearchInputFacetedAttributeResult() {
		super(FacetFiltering.INPUT);
	}

	public SearchInputFacetedAttributeResult(String id, String name, Integer noAttributeValueCount) {
		super(id, name, FacetFiltering.INPUT, noAttributeValueCount);
	}
}
