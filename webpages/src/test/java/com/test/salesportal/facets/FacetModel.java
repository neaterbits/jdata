package com.test.salesportal.facets;

import com.test.salesportal.jsutils.JSInvocable;
import com.test.salesportal.jsutils.JavaWrapper;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;

final class FacetModel extends JavaWrapper {

	FacetModel(JSInvocable invocable, Object jsObject) {
		super(invocable, jsObject);
	}
	
	public void updateFacets(SearchFacetsResult facets) {
	
		this.invokeMethod("updateFacets", facets);
	}
 	
	public String getTypeId(int typeIdx) {
		return (String)invokeMethod("getTypeId", typeIdx);
	}
}
