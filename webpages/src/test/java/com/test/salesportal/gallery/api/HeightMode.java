package com.test.salesportal.gallery.api;

public interface HeightMode {

	int computeHeight(GalleryConfig config, int rowSpacing, int numColumns, int totalNumberOfItems);
}
