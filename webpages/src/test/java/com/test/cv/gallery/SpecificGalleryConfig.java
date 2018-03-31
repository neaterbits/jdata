package com.test.cv.gallery;

public class SpecificGalleryConfig extends GalleryConfig {
	
	private final int width;
	private final int height;

	SpecificGalleryConfig(int columnSpacing, int rowSpacing, int width, int height) {
		super(columnSpacing, rowSpacing);
		
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
