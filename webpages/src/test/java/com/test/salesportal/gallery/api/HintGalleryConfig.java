package com.test.salesportal.gallery.api;

public class HintGalleryConfig extends GalleryConfig {

	private final int widthHint;
	private final int heightHint;
	
	public HintGalleryConfig(int columnSpacing, int rowSpacing, int widthHint, int heightHint) {
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
