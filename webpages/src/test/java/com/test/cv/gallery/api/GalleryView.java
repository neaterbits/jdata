package com.test.cv.gallery.api;

/**
 * Same as JS interface
 */
public interface GalleryView<CONTAINER, ELEMENT,
	RENDER_CONTAINER extends CONTAINER,
	PLACEHOLDER extends CONTAINER,
	ROW extends CONTAINER,
	ITEM extends ELEMENT,
	PROVISIONAL extends ITEM,
	COMPLETE extends ITEM
	> {
	
	/**
	 * Create view element (a <div> typically)
	 * that will take up space at start of view so that
	 * if eg. first visible item is element at index 100, this will take
	 * up all non-visible space before that so that scrollbars are show at right place
	 * and size even if we are really just rendering the items that are visible right now
	 * 
	 * @return element to take up space, a <div> element if this is web
	 */
	PLACEHOLDER createUpperPlaceHolder();


	/**
	 * Create eg. div element for keeping all elements in a row
	 * 
	 * @return div
	 */
	ROW createRowContainer(int rowNo);

	/**
	 * Append the placeholder to scrollable render container
	 * 
	 * @param container the outer scrollable render area
	 * @param placeholder the placeholder to add, previously created with createUpperPlaceHolder()
	 */
	void appendPlaceholderToRenderContainer(RENDER_CONTAINER container, PLACEHOLDER placeholder);
	
	
	/**
	 * Append a row to scrollable render container
	 * 
	 * @param container the outer scrollable render area
	 * @param row the row to add, previously created with createRowContainer()
	 */
	void appendRowToRenderContainer(RENDER_CONTAINER container, ROW row);
	
	/**
	 * Prepend a row to scrollable render container.
	 * Note, this is added after the placeholder that push rows down to currently visible area.
	 * 
	 * @param container the outer scrollable render area
	 * @param row the row to add, previously created with createRowContainer()
	 */
	
	void prependRowToRenderContainer(RENDER_CONTAINER container, ROW rowToAdd, ROW currentFirstRow);
	
	/**
	 * Append an item to a row
	 * 
	 * @param row the row to append to, previously created with createRowContainer()
	 * @param item the item to add, ought to be provisional since complete-items are only replaced from existings
	 */
	
	void appendItemToRowContainer(ROW row, PROVISIONAL item);
	
	PROVISIONAL makeProvisionalHTMLElement(int index, Object data);

	COMPLETE makeCompleteHTMLElement(int index, Object provisionalData, Object completeData);

	void applyItemStyles(ITEM element, Integer rowHeight, Integer itemWidth, Integer itemHeight, int spacing, boolean visible);

	void applyRowContainerStyling(ROW rowContainer, int y, int width, int height);

	/**
	 * Replace provisional with complete when complete has loade
	 * @param container container element
	 * @param index the index into row for element to replace
	 * @param element the element the provisional is to be replaced with
	 */
	void replaceProvisionalWithComplete(ROW container, int index, COMPLETE element);

	/**
	 * Called whenever doing complete redraw or when scrolling and 
	 * removing rows furthermost from visible area in order to keep scrolling fast
	 * 
	 * @param container render div container
	 * @param element row
	 */
	
	void removeRowFromContainer(RENDER_CONTAINER container, ROW element);

	void setRenderContainerHeight(RENDER_CONTAINER element, int heightPx);
	
	void setPlaceHolderHeight(PLACEHOLDER element, int heightPx);

}

