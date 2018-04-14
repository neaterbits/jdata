package com.test.cv.gallery.stubs.galleryview;

import java.util.ArrayList;
import java.util.List;

import com.test.cv.gallery.stubs.html.Div;

/**
 * Corresponding to DOM element for gallery row element, separate class
 * for type safety.
 */

public class Row extends Div {
	private final int rowNo;

	public Row(int rowNo) {
		this.rowNo = rowNo;
	}

	public int getRowNo() {
		return rowNo;
	}

	public <T extends Item> List<T> getElements(Class<T> type) {

		final int num = getNumElements();
		final List<T> elements = new ArrayList<>(num);
		
		for (int i = 0; i < num; ++ i) {
			@SuppressWarnings("unchecked")
			final T element = (T)getElement(i);

			elements.add(element);
		}

		return elements;
	}
}
