package com.test.cv.facets;

final class ViewTypeList extends ViewList<ViewTypeContainer> {
	
	private final boolean isRoot;

	ViewTypeList(ViewContainer<?> parentElement, boolean isRoot) {
		super(parentElement);
		
		this.isRoot = isRoot;
	}

	boolean isRoot() {
		return isRoot;
	}
}
