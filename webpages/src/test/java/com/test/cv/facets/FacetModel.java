package com.test.cv.facets;

import com.test.cv.jsutils.JSInvocable;
import com.test.cv.jsutils.JavaWrapper;
import com.test.cv.rest.SearchFacetsResult;

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
