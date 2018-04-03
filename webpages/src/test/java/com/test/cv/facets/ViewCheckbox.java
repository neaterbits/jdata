package com.test.cv.facets;

final class ViewCheckbox extends ViewElement{

	private Object onClicked;
	
	ViewCheckbox(ViewContainer parentElement) {
		super(parentElement);
	
		if (parentElement == null) {
			throw new IllegalArgumentException("parentElement == null");
		}
	}

	Object getOnClicked() {
		return onClicked;
	}

	void setOnClicked(Object onClicked) {

		if (onClicked == null) {
			throw new IllegalArgumentException("onClicked == null");
		}

		this.onClicked = onClicked;
	}
}
