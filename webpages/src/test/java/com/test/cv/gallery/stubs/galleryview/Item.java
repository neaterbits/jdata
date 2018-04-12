package com.test.cv.gallery.stubs.galleryview;

import com.test.cv.gallery.stubs.html.Div;

/**
 * Corresponding to DOM element for gallery item, separate class
 * for type safety.
 */

public abstract class Item extends Div {

	private final int index; // Index into virtual array of items
	
	public Item(int index) {
		this.index = index;
	}

	public Item(Integer width, Integer height, int index) {
		super(width, height);

		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
