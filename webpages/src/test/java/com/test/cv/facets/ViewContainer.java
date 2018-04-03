package com.test.cv.facets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class ViewContainer<SUB extends ViewElement> extends ViewElement {

	private final List<SUB> subElements;
	
	ViewContainer(ViewContainer<?> parentElement) {
		super(parentElement);
		
		this.subElements = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	final void addSubElement(ViewElement subElement) {
		if (subElement == null) {
			throw new IllegalArgumentException("subElement == null");
		}
		
		this.subElements.add((SUB)subElement);
	}
	
	final List<SUB> getSubElements() {
		return Collections.unmodifiableList(this.subElements);
	}
}
