package com.test.cv.gallery;

import com.test.cv.jsutils.JSInvocable;

/**
 * Wraps the JS instance
 */
public class GalleryCacheAllProvisionalSomeComplete extends GalleryCacheBase {

	private final GalleryModel galleryModel;
	private final GalleryView<?, ?> galleryView;

	GalleryCacheAllProvisionalSomeComplete(
			JSInvocable runtime,
			Object jsObject,
			GalleryModel galleryModel,
			GalleryView<?, ?> galleryView) {
		
		super(runtime, jsObject);
		
		if (runtime == null) {
			throw new IllegalArgumentException("runtime == null");
		}
		
		if (galleryModel == null) {
			throw new IllegalArgumentException("galleryModel == null");
		}
		
		if (galleryView == null) {
			throw new IllegalArgumentException("galleryView == null");
		}
		

		this.galleryModel = galleryModel;
		this.galleryView = galleryView;
	}
	
	void refresh(int totalNumberOfItems) {
		
		invokeMethod("refresh", 0, totalNumberOfItems);
		
	}
	
	/**
	 * To be passed in if are JS instances
	 * @param totalNumberOfItems
	 * @param widthMode
	 * @param heightMode
	 */

	void refreshWithJSObjs(int totalNumberOfItems) {
		
		invokeMethod("refresh", 0, totalNumberOfItems);
		
	}
}

