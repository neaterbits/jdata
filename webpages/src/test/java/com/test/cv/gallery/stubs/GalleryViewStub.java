package com.test.cv.gallery.stubs;

import java.util.function.BiFunction;

import com.test.cv.gallery.api.GalleryView;
import com.test.cv.gallery.stubs.galleryview.Complete;
import com.test.cv.gallery.stubs.galleryview.Item;
import com.test.cv.gallery.stubs.galleryview.Placeholder;
import com.test.cv.gallery.stubs.galleryview.Provisional;
import com.test.cv.gallery.stubs.galleryview.RenderDiv;
import com.test.cv.gallery.stubs.galleryview.Row;
import com.test.cv.gallery.stubs.html.Div;
import com.test.cv.gallery.stubs.html.Element;
import com.test.cv.gallery.stubs.modeldata.CompleteData;
import com.test.cv.gallery.stubs.modeldata.ElementSize;
import com.test.cv.gallery.stubs.modeldata.ProvisionalData;

public class GalleryViewStub extends GalleryViewElementsStub implements GalleryView<
		Div, Element,
		RenderDiv, Placeholder, Row, Item, Provisional, Complete> {

	private final Placeholder upperPlaceHolder;
	
	public GalleryViewStub() {
		this.upperPlaceHolder = new Placeholder();
	}
	
	@Override
	public Placeholder createUpperPlaceHolder() {
		return upperPlaceHolder;
	}

	@Override
	public Row createRowContainer() {
		return new Row();
	}
	
	@Override
	public void appendPlaceholderToRenderContainer(RenderDiv container, Placeholder placeholder) {
		super.appendToContainer(container, placeholder);
	}

	@Override
	public void appendRowToRenderContainer(RenderDiv container, Row row) {
		super.appendToContainer(container, row);
	}

	@Override
	public void appendItemToRowContainer(Row row, Provisional item) {
		super.appendToContainer(row, item);
	}

	private static <T extends Item> T makeGalleryItemDiv(ElementSize size, BiFunction<Integer, Integer, T> constructor) {
		return makeGalleryItemDiv(size.getWidth(), size.getHeight(), constructor);
	}

	private static <T extends Item> T makeGalleryItemDiv(Integer width, Integer height, BiFunction<Integer, Integer, T> constructor) {
		final T div = constructor.apply(width, height);
		
		// Simulate image div, adding an image and title element
		div.append(new Element());
		div.append(new Element());

		return div;
	}

	@Override
	public Provisional makeProvisionalHTMLElement(int index, Object data) {
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}
		
		final Provisional element;
		if (data instanceof ProvisionalData) {

			final ProvisionalData size = (ProvisionalData)data;

			element = makeGalleryItemDiv(size, Provisional::new);
		}
		else {
			element = new Provisional();
		}
		
		return element;
	}

	
	@Override
	public Complete makeCompleteHTMLElement(int index, Object provisionalData, Object completeData) {

		final CompleteData c = (CompleteData)completeData;

		return makeGalleryItemDiv(c, Complete::new);
	}

	@Override
	public void applyItemStyles(Item element, Integer rowHeight, Integer itemWidth, Integer itemHeight,
			int spacing, boolean visible) {

		if (itemWidth != null) {
			element.setWidth(itemWidth);
		}
		
		if (itemHeight != null) {
			element.setHeight(itemHeight);
		}
	}

	@Override
	public void applyRowContainerStyling(Row rowContainer, int y, int width, int height) {
		rowContainer.setWidth(width);
		rowContainer.setHeight(height);
	}

	@Override
	public void replaceProvisionalWithComplete(Row container, int index, Provisional element) {
		replaceElement(container, index, element);
	}
}
