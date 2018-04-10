package com.test.cv.gallery.stubs;

import com.test.cv.gallery.api.GalleryView;
import com.test.cv.gallery.stubs.html.Div;
import com.test.cv.gallery.stubs.html.Element;

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

	@Override
	public Element makeProvisionalHTMLElement(int index, Object data) {
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}

		return new Element();
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
