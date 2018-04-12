package com.test.cv.gallery.wrappers;

import com.test.cv.jsutils.JSInvocable;
import com.test.cv.jsutils.JavaWrapper;

public class GalleryCacheBase extends JavaWrapper {
	
	public GalleryCacheBase(JSInvocable invocable, Object jsObject) {
		super(invocable, jsObject);
	}

	/**
	 * Set the element which gallery will be rendered into
	 * @param outer the outer element that is itself not scrollable but the inner element will be scrollable within this element
	 * @param inner this element is where we will add gallery rows. It will have heigt to set of height of all rows (or an approximzation of that heght) and be scrollable within
	 *              the outer element.
	 * 
	 */
	
	public void setGalleryDivs(Object outer, Object inner) {
		invokeMethod("setGalleryDivs", outer, inner);
	}
	
	public int computeIndexOfLastOnRowStartingWithIndexWithArgs(int indexOfFirstInRow, int numColumns, int totalNumberOfItems) {
		final Object o = invokeMethod("_computeIndexOfLastOnRowStartingWithIndexWithArgs", indexOfFirstInRow, numColumns, totalNumberOfItems);
		
		return nonNullNumberToExactInt(o);
	}
	
	public int whiteboxGetTotalNumberOfItems() {
		return nonNullNumberToExactInt(invokeMethod("_getTotalNumberOfItems"));
	}
	
	public int whiteboxComputeNumRowsTotalFromNumColumns(int numColumns) {
		return nonNullNumberToExactInt(invokeMethod("_computeNumRowsTotalFromNumColumns", numColumns));
	}
}
