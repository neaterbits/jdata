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
	ROW createRowContainer();

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
	
	
}

