package com.test.cv.gallery.stubs.galleryview;

/**
 * Corresponding to DOM element for provisional gallery item element, separate class
 * for type safety.
 */

public class Provisional extends Item {

	public Provisional(int index) {
		super(index);
	}

	public Provisional(Integer width, Integer height, int index) {
		super(width, height, index);
	}
}
