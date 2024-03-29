package com.test.salesportal.facets;

abstract class ViewElement {
	private final ViewContainer<?> parentElement;

	ViewElement(ViewContainer<?> parentElement) {
		this.parentElement = parentElement;
		
		if (parentElement != null) {
			parentElement.addSubElement(this);
		}
	}

	final void checkText(String text) {
		if (text == null) {
			throw new IllegalArgumentException("text == null");
		}
		
		final String trimmed = text.trim();
		
		if (!trimmed.equals(text)) {
			throw new IllegalArgumentException("whitespace at beginning or end of text");
		}

		if (text.isEmpty()) {
			throw new IllegalArgumentException("Text is empty");
		}
	}
	
	final ViewContainer<?> getParentElement() {
		return parentElement;
	}
}
