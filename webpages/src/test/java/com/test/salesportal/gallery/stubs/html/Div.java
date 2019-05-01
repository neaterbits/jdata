package com.test.salesportal.gallery.stubs.html;

import java.util.ArrayList;
import java.util.List;

public class Div extends Element {
	
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

	public void insertBefore(Element toAdd, Element beforeThis) {
		if (toAdd == null) {
			throw new IllegalArgumentException("toADd == null");
		}
		
		if (beforeThis == null) {
			throw new IllegalArgumentException("beforeThis == null");
		}
		
		final int index = elements.indexOf(beforeThis);
		
		if (index < 0) {
			throw new IllegalArgumentException("beforeThis not found"); 
		}
		
		final ArrayList<Element> newElements = new ArrayList<>();
		if (index > 0) {
			newElements.addAll(elements.subList(0, index));
		}
		newElements.add(toAdd);
		newElements.addAll(elements.subList(index, elements.size()));
		
		this.elements.clear();
		this.elements.addAll(newElements);
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
	
	public void removeElement(Element element) {
		if (element == null) {
			throw new IllegalArgumentException("element == null");
		}

		if (!elements.remove(element)) {
			throw new IllegalStateException("Element was not in collection");
		}
	}
}
