package com.test.salesportal.facets;

import com.test.salesportal.jsutils.JSInvocable;
import com.test.salesportal.jsutils.JavaWrapper;

/**
 * Wrapper for facetcontroller.js
 */
final class FacetController extends JavaWrapper {

	private final FacetModel model;
	private final FacetView view;
	
	public FacetController(JSInvocable invocable, Object jsObject, FacetModel model, FacetView view) {
		super(invocable, jsObject);

		if (model == null) {
			throw new IllegalArgumentException("model == null");
		}

		if (view == null) {
			throw new IllegalArgumentException("view == null");
		}

		this.model = model;
		this.view = view;
	}

	void refresh() {
		invokeMethod("refresh");
	}

	FacetModel getModel() {
		return model;
	}

	FacetView getView() {
		return view;
	}
}
