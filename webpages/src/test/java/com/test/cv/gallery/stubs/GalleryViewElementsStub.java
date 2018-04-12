package com.test.cv.gallery.stubs;

import com.test.cv.gallery.api.GalleryViewElements;
import com.test.cv.gallery.stubs.html.Div;
import com.test.cv.gallery.stubs.html.Element;

public class GalleryViewElementsStub implements GalleryViewElements<Div, Element> {

	final void appendToContainer(Div container, Element toAdd) {
		container.append(toAdd);
	}

	@Override
	public int getNumElements(Div container) {
		return container.getNumElements();
	}

	@Override
	public Element getElement(Div container, int index) {
		return container.getElement(index);
	}

	@Override
	public void replaceElement(Div container, int index, Element element) {
		if (element == null) {
			throw new IllegalArgumentException("element == null");
		}

		container.replaceElement(index, element);
	}

	@Override
	public void removeElement(Div container, Element element) {
		container.removeElement(element);
	}

	@Override
	public int getElementWidth(Element element) {
		return element.getWidth();
	}

	@Override
	public int getElementHeight(Element element) {
		return element.getHeight();
	}

	@Override
	public void setElementHeight(Element element, int heightPx) {
		element.setHeight(heightPx);
	}

	@Override
	public void setCSSClasses(Element element, String classes) {
		// Nothing for now, this is for reference from CSS
	}
}