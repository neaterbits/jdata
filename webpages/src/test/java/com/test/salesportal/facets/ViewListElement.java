package com.test.salesportal.facets;

abstract class ViewListElement<SUB extends ViewElement> extends ViewContainer<SUB> {

	private final String text;

	ViewListElement(ViewContainer<?> parentElement, String text) {
		super(parentElement);

		checkText(text);
		
		this.text = text;
	}

	String getText() {
		return text;
	}
}
