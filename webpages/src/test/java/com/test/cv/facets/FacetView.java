package com.test.cv.facets;

import com.test.cv.jsutils.JSInvocable;
import com.test.cv.jsutils.JavaWrapper;

public class FacetView extends JavaWrapper {

	public FacetView(JSInvocable invocable, Object jsObject) {
		super(invocable, jsObject);
	}

	public void initFromModel(FacetModel model) {
		this.invokeMethod("initFromModel", model);
	}
	
	public void refreshFromNewModel(FacetModel model) {
		this.invokeMethod("refreshFromNewModel", model);
	}
	
	public void collectCriteriaAndTypesFromSelections() {
		throw new UnsupportedOperationException("TODO");
	}
}

