package com.test.cv.gallery.stubs;

import com.test.cv.gallery.api.GalleryView;
import com.test.cv.gallery.stubs.html.Div;
import com.test.cv.gallery.stubs.html.Element;
import com.test.cv.gallery.stubs.modeldata.CompleteData;
import com.test.cv.gallery.stubs.modeldata.ElementSize;
import com.test.cv.gallery.stubs.modeldata.ProvisionalData;

public class GalleryViewStub implements GalleryView<Div, Element> {

	private final Div upperPlaceHolder;
	
	public GalleryViewStub() {
		this.upperPlaceHolder = new Div();
	}
	
	@Override
	public Div createUpperPlaceHolder() {
		return upperPlaceHolder;
	}

	@Override
	public void appendToContainer(Div container, Element toAdd) {
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
	public Div createRowContainer() {
		return new Div();
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

	private static Div makeGalleryItemDiv(ElementSize size) {
		return makeGalleryItemDiv(size.getWidth(), size.getHeight());
	}

	private static Div makeGalleryItemDiv(Integer width, Integer height) {
		final Div div = new Div(width, height);
		
		// Simulate image div, adding an image and title element
		div.append(new Element());
		div.append(new Element());

		return div;
	}

	@Override
	public Element makeProvisionalHTMLElement(int index, Object data) {
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}
		
		final Element element;
		if (data instanceof ProvisionalData) {

			final ProvisionalData size = (ProvisionalData)data;

			element = makeGalleryItemDiv(size);
		}
		else {
			element = new Element();
		}
		
		return element;
	}

	
	@Override
	public Element makeCompleteHTMLElement(int index, Object provisionalData, Object completeData) {

		final CompleteData c = (CompleteData)completeData;

		return makeGalleryItemDiv(c);
	}

	@Override
	public void applyItemStyles(Element element, Integer rowHeight, Integer itemWidth, Integer itemHeight,
			int spacing, boolean visible) {

		if (itemWidth != null) {
			element.setWidth(itemWidth);
		}
		
		if (itemHeight != null) {
			element.setHeight(itemHeight);
		}
	}

	@Override
	public void applyRowContainerStyling(Div rowContainer, int y, int width, int height) {
		rowContainer.setWidth(width);
		rowContainer.setHeight(height);
	}
}
