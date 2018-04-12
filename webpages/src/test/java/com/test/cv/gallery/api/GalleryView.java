package com.test.cv.gallery.api;

/**
 * Same as JS interface
 */
public interface GalleryView<CONTAINER, ELEMENT,
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
	
	
	PROVISIONAL makeProvisionalHTMLElement(int index, Object data);

	COMPLETE makeCompleteHTMLElement(int index, Object provisionalData, Object completeData);

	void applyItemStyles(ITEM element, Integer rowHeight, Integer itemWidth, Integer itemHeight, int spacing, boolean visible);

	void applyRowContainerStyling(ROW rowContainer, int y, int width, int height);
}

