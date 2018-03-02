package com.test.cv.rest;

import java.util.List;

public class SearchSingleValueFacetedAttributeResult extends SearchFacetedAttributeResult {

	private List<SearchSingleValueFacet> values;

	public List<SearchSingleValueFacet> getValues() {
		return values;
	}

	public void setValues(List<SearchSingleValueFacet> values) {
		this.values = values;
	}
}
