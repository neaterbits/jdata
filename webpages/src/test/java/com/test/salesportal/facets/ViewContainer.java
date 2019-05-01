package com.test.salesportal.facets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class ViewContainer<SUB extends ViewElement> extends ViewElement {

	private final List<SUB> subElements;
	private final List<ViewCheckbox> checkboxes;
	
	ViewContainer(ViewContainer<?> parentElement) {
		super(parentElement);
		
		this.subElements = new ArrayList<>();
		this.checkboxes = new ArrayList<>();
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
	
	final void removeSubElement(Object sub) {
		this.subElements.remove(sub);
	}
	
	final void addCheckbox(ViewCheckbox checkbox) {
		if (checkbox == null) {
			throw new IllegalArgumentException("checkbox == null");
		}
		
		this.checkboxes.add(checkbox);
	}

	final List<ViewCheckbox> getCheckboxes() {
		return Collections.unmodifiableList(this.checkboxes);
	}
}
