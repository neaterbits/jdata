package com.test.cv.facets;

abstract class ViewListElement extends ViewContainer {

	private final String text;

	ViewListElement(ViewContainer parentElement, String text) {
		super(parentElement);

		checkText(text);
		
		this.text = text;
	}

	String getText() {
		return text;
	}
}
