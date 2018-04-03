package com.test.cv.facets;

abstract class ViewList<SUB extends ViewElement> extends ViewSubContainer<SUB> {

	ViewList(ViewContainer<?> parentElement) {
		super(parentElement);
	}
}
