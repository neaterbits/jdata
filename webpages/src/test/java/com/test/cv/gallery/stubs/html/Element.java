package com.test.cv.gallery.stubs.html;

public class Element {
	private Integer width;
	private Integer height;
	
	public Element() {
		this(null, null);
	}

	public Element(Integer width, Integer height) {
		this.width = width;
		this.height = height;
	}
	
	public final Integer getWidth() {
		return width;
	}
	
	public final void setWidth(int width) {
		this.width = width;
	}

	public final Integer getHeight() {
		return height;
	}

	public final void setHeight(int height) {
		this.height = height;
	}
}
