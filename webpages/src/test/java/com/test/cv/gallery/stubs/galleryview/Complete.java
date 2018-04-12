package com.test.cv.gallery.stubs.galleryview;

/**
 * Corresponding to DOM element for provisional gallery item element, separate class
 * for type safety.
 */

public class Complete extends Item {

	public Complete(int index) {
		super(index);
	}

	public Complete(Integer width, Integer height, int index) {
		super(width, height, index);
	}
}
