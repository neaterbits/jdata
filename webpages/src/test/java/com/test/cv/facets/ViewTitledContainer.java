package com.test.cv.facets;

abstract class ViewTitledContainer<SUB extends ViewElement> extends ViewSubContainer<SUB> {

	private final String text;

	ViewTitledContainer(ViewContainer<?> parentElement, String text) {
		super(parentElement);

		checkText(text);

		this.text = text;
	}

	final String getText() {
		return text;
	}
}

