package com.test.cv.gallery;

public abstract class GalleryConfig {

	private final int columnSpacing;
	private final int rowSpacing;
	
	GalleryConfig(int columnSpacing, int rowSpacing) {
		this.columnSpacing = columnSpacing;
		this.rowSpacing = rowSpacing;
	}

	public final int getColumnSpacing() {
		return columnSpacing;
	}

	public final int getRowSpacing() {
		return rowSpacing;
	}
}
