package com.test.salesportal.facets;

abstract class ViewList<SUB extends ViewElement> extends ViewSubContainer<SUB> {

	ViewList(ViewContainer<?> parentElement) {
		super(parentElement);
	}
}
