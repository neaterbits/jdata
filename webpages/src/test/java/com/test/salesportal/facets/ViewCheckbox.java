package com.test.salesportal.facets;

final class ViewCheckbox {

	private Object onClicked;
	
	ViewCheckbox(ViewContainer<?> parentElement) {
	
		if (parentElement == null) {
			throw new IllegalArgumentException("parentElement == null");
		}
		
		parentElement.addCheckbox(this);
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
