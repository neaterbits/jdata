package com.test.cv.gallery.stubs.galleryview;

import com.test.cv.gallery.stubs.html.Div;

/**
 * Corresponding to DOM element for gallery item, separate class
 * for type safety.
 */

public abstract class Item extends Div {

	public Item() {

	}

	public Item(Integer width, Integer height) {
		super(width, height);
	}
}
