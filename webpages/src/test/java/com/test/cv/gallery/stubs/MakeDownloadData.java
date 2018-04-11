package com.test.cv.gallery.stubs;

@FunctionalInterface
public interface MakeDownloadData {
	
	/**
	 * Call on gallery model to get data
	 * 
	 * @param startIndex index into data model virtual array
	 * @param count number of items
	 * @param indexInDownloadSet index into set to be downloaded or updated
	 *        (ie. we only download the visible set of the virtual array and if we are downloading 20 items, this would be 0 to 19).
	 *        Used for debug purposes, to see what number this item is in set to be downloaded.
	 * 
	 * @return item.
	 */
	Object makeDownloadData(int startIndex, int count, int indexInDownloadSet);
}
