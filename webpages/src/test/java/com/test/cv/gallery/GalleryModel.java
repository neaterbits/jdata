package com.test.cv.gallery;

/**
 * Same as user-specified model called from JS
 */
public interface GalleryModel {

	void getProvisionalData(int index, int count, Object onSuccess);

	void getCompleteData(int index, int count, Object onSuccess);
}
