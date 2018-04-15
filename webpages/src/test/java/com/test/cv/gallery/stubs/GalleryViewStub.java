package com.test.cv.gallery.stubs;

import java.util.ArrayList;
import java.util.List;

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

public final class GalleryViewStub extends GalleryViewElementsStub implements GalleryView<
		Div, Element,
		RenderDiv, Placeholder, Row, Item, Provisional, Complete> {

	private final Placeholder upperPlaceHolder;
	
	// For test assertions on operations
	private final GalleryViewOperations operations;
	
	private final List<Row> rows;
	
	public GalleryViewStub() {
		this.upperPlaceHolder = new Placeholder();
		
		this.rows = new ArrayList<>();
		this.operations = new GalleryViewOperations();
	}
	
	@Override
	public Placeholder createUpperPlaceHolder() {
		
		operations.createUpperPlaceHolder();
		
		return upperPlaceHolder;
	}

	@Override
	public Row createRowContainer(int rowNo) {
		
		operations.createRowContainer(rowNo);
		
		final Row row = new Row(rowNo);
		
		if (rowNo >= rows.size()) {
			final int toAdd = rowNo - rows.size() + 1;

			for (int i = 0; i < toAdd; ++ i) {
				rows.add(null);
			}
		}

		// Should clear before adding new row at same position
		if (rows.get(rowNo) != null) {
			throw new IllegalStateException("Already has row at " + rowNo);
		}
		
		this.rows.set(rowNo, row);
		
		return row;
	}
	
	@Override
	public void appendPlaceholderToRenderContainer(RenderDiv container, Placeholder placeholder) {
		operations.appendPlaceholderToRenderContainer();

		super.appendToContainer(container, placeholder);
	}

	@Override
	public void appendRowToRenderContainer(RenderDiv container, Row row) {
		operations.appendRowToRenderContainer(row.getRowNo());
		
		super.appendToContainer(container, row);
	}
	
	

	@Override
	public void prependRowToRenderContainer(RenderDiv container, Row rowToAdd, Row currentFirstRow) {
		
		if (rowToAdd.getRowNo() != currentFirstRow.getRowNo() - 1) {
			throw new IllegalArgumentException("Row is not current - 1");
		}

		operations.prependRowToRenderContainer(rowToAdd.getRowNo());

		super.insertBefore(container, rowToAdd, currentFirstRow);
	}

	@Override
	public void appendItemToRowContainer(Row row, Provisional item) {
		operations.appendItemToRowContainer(row.getRowNo(), row.getNumElements(), item.getIndex());
		
		super.appendToContainer(row, item);
	}

	public final GalleryViewOperations getOperations() {
		return operations;
	}

	@FunctionalInterface
	interface ItemContructor<T> {
		T construct(Integer width, Integer height, int index);
	}

	private static <T extends Item> T makeGalleryItemDiv(ElementSize size, int index, ItemContructor<T> constructor) {
		return makeGalleryItemDiv(size.getWidth(), size.getHeight(), index, constructor);
	}

	private static <T extends Item> T makeGalleryItemDiv(Integer width, Integer height, int index, ItemContructor<T> constructor) {
		final T div = constructor.construct(width, height, index);
		
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

			element = makeGalleryItemDiv(size, index, Provisional::new);
		}
		else {
			element = new Provisional(index);
		}
		
		return element;
	}

	
	@Override
	public Complete makeCompleteHTMLElement(int index, Object provisionalData, Object completeData) {

		final CompleteData c = (CompleteData)completeData;

		return makeGalleryItemDiv(c, index, Complete::new);
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
	public void replaceProvisionalWithComplete(Row container, int indexIntoRow, Complete element) {
		
		operations.replaceProvisionalWithComplete(container.getRowNo(), indexIntoRow, element.getIndex());
		
		final Provisional current = (Provisional)container.getElement(indexIntoRow);
		
		if (current.getIndex() != element.getIndex()) {
			throw new IllegalArgumentException("Index mismatch in replacement: " + current.getIndex() + "/" + element.getIndex());
		}

		replaceElement(container, indexIntoRow, element);
	}

	@Override
	public void removeRowFromContainer(RenderDiv container, Row element) {

		operations.removeRowFromContainer(element.getRowNo());
		
		super.removeElement(container, element);
	}
	
	@Override
	public void setRenderContainerHeight(RenderDiv element, int heightPx) {
		super.setElementHeight(element, heightPx);
	}

	@Override
	public void setPlaceHolderHeight(Placeholder element, int heightPx) {

		operations.setPlaceHolderHeight(heightPx);

		super.setElementHeight(element, heightPx);
	}

	public Row getRow(int rowNo) {
		return rows.get(rowNo);
	}
	
	public <T extends Item> List<T> getElements(int rowNo, Class<T> type) {
		return getRow(rowNo).getElements(type);
	}
}
