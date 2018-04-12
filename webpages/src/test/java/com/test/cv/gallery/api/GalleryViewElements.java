package com.test.cv.gallery.api;

/**
 * Base interface for accessing element tree (abstraction for DOM tree)
 */
public interface GalleryViewElements<CONTAINER, ELEMENT> {
	/**
	 * Append to container element (eg. a div element)
	 * 
	 * @param container container element
	 * @param toAdd element to add
	 */
	
	int getNumElements(CONTAINER container);

	ELEMENT getElement(CONTAINER container, int index);
	
	void replaceElement(CONTAINER container, int index, ELEMENT element);
	
	void removeElement(CONTAINER container, ELEMENT element);

	int getElementWidth(ELEMENT element);
	
	int getElementHeight(ELEMENT element);
	
	void setElementHeight(ELEMENT element, int heightPx);
	
	void setCSSClasses(ELEMENT element, String classes);

}
