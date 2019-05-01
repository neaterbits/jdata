package com.test.salesportal.gallery.api;

public abstract class GalleryConfig {

	private final int columnSpacing;
	private final int rowSpacing;
	
	protected GalleryConfig(int columnSpacing, int rowSpacing) {
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
