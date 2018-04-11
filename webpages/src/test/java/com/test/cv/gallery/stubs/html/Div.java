package com.test.cv.gallery.stubs.html;

import java.util.ArrayList;
import java.util.List;

public final class Div extends Element {
	
	private final List<Element> elements;
	
	public Div() {
		this(null, null);
	}

	public Div(Integer width, Integer height) {
		super(width, height);

		this.elements = new ArrayList<>();
	}
	
	public void append(Element element) {
		if (element == null) {
			throw new IllegalArgumentException("element == null");
		}
		
		elements.add(element);
	}
	
	public int getNumElements() {
		return elements.size();
	}

	public Element getElement(int index) {
		return elements.get(index);
	}

	public void replaceElement(int index, Element element) {
		if (element == null) {
			throw new IllegalArgumentException("element == null");
		}

		if (index >= elements.size()) {
			throw new IllegalArgumentException("Index out of range");
		}

		elements.set(index, element);
	}
}
