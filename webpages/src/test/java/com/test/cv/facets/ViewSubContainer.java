package com.test.cv.facets;

/**
 * Sub-container, ie non-root container thus has a parentElement
 */
abstract class ViewSubContainer extends ViewContainer {

	ViewSubContainer(ViewContainer parentElement) {
		super(parentElement);

		if (parentElement == null) {
			throw new IllegalArgumentException("parentElement == null");
		}
	}
}
