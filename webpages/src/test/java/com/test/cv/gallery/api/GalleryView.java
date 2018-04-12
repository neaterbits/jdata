package com.test.cv.gallery.api;

/**
 * Same as JS interface
 */
public interface GalleryView<CONTAINER, ELEMENT> {
	
	/**
	 * Create view element (a <div> typically)
	 * that will take up space at start of view so that
	 * if eg. first visible item is element at index 100, this will take
	 * up all non-visible space before that so that scrollbars are show at right place
	 * and size even if we are really just rendering the items that are visible right now
	 * 
	 * @return element to take up space, a <div> element if this is web
	 */
	CONTAINER createUpperPlaceHolder();

	/**
	 * Append to container element (eg. a div element)
	 * 
	 * @param container container element
	 * @param toAdd element to add
	 */
	
	void appendToContainer(CONTAINER container, ELEMENT toAdd);
	
	int getNumElements(CONTAINER container);

	ELEMENT getElement(CONTAINER container, int index);
	
	void replaceElement(CONTAINER container, int index, ELEMENT element);
	
	void removeElement(CONTAINER container, ELEMENT element);

	/**
	 * Create eg. div element for keeping all elements in a row
	 * 
	 * @return div
	 */
	CONTAINER createRowContainer();
	
	int getElementWidth(ELEMENT element);
	
	int getElementHeight(ELEMENT element);
	
	void setElementHeight(ELEMENT element, int heightPx);
	
	void setCSSClasses(ELEMENT element, String classes);
	
	ELEMENT makeProvisionalHTMLElement(int index, Object data);

	ELEMENT makeCompleteHTMLElement(int index, Object provisionalData, Object completeData);

	void applyItemStyles(ELEMENT element, Integer rowHeight, Integer itemWidth, Integer itemHeight, int spacing, boolean visible);

	void applyRowContainerStyling(CONTAINER rowContainer, int y, int width, int height);
}

