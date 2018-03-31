package com.test.cv.gallery;

public class HintGalleryConfig extends GalleryConfig {

	private final int widthHint;
	private final int heightHint;
	
	HintGalleryConfig(int columnSpacing, int rowSpacing, int widthHint, int heightHint) {
		super(columnSpacing, rowSpacing);

		this.widthHint = widthHint;
		this.heightHint = heightHint;
	}

	public int getWidthHint() {
		return widthHint;
	}

	public int getHeightHint() {
		return heightHint;
	}
}
