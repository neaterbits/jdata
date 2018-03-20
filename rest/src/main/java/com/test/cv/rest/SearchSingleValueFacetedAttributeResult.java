package com.test.cv.rest;

import java.util.List;

public class SearchSingleValueFacetedAttributeResult extends SearchFacetedAttributeResult {

	private List<SearchSingleValueFacet> values;
	
	private Integer noAttributeValueCount;

	public List<SearchSingleValueFacet> getValues() {
		return values;
	}

	public void setValues(List<SearchSingleValueFacet> values) {
		this.values = values;
	}

	public Integer getNoAttributeValueCount() {
		return noAttributeValueCount;
	}

	public void setNoAttributeValueCount(int noAttributeValueCount) {
		this.noAttributeValueCount = noAttributeValueCount;
	}
}
