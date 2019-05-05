package com.test.salesportal.rest.search.model.facetresult;

import java.util.List;

public class SearchFacetsResult {

	private List<SearchFacetedTypeResult> types;

	public List<SearchFacetedTypeResult> getTypes() {
		return types;
	}

	public void setTypes(List<SearchFacetedTypeResult> types) {
		this.types = types;
	}
}
