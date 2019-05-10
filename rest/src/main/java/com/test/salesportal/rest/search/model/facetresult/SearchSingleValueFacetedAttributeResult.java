package com.test.salesportal.rest.search.model.facetresult;

import java.util.List;

import com.test.salesportal.model.FacetFiltering;

public class SearchSingleValueFacetedAttributeResult extends SearchFacetedAttributeResult {

	public SearchSingleValueFacetedAttributeResult() {
		super(FacetFiltering.VALUE);
	}

	public SearchSingleValueFacetedAttributeResult(String id, String name, Integer noAttributeValueCount) {
		super(id, name, FacetFiltering.VALUE, noAttributeValueCount);
	}

	private List<SearchSingleValueFacet> values;
	
	public List<SearchSingleValueFacet> getValues() {
		return values;
	}

	public void setValues(List<SearchSingleValueFacet> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "SearchSingleValueFacetedAttributeResult [values=" + values + ", getId()=" + getId() + ", getName()="
				+ getName() + "]";
	}
}
