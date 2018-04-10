package com.test.cv.gallery.stubs.html;

public class ElementSize {
	private final Integer width;
	private final Integer height;

	public ElementSize(Integer width, Integer height) {
		this.width = width;
		this.height = height;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return "" + width + "x" + height;
	}
	
	
}
