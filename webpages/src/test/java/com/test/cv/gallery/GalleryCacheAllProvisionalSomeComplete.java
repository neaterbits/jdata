package com.test.cv.gallery;

import com.test.cv.jsutils.JSInvocable;

/**
 * Wraps the JS instance
 */
public class GalleryCacheAllProvisionalSomeComplete extends GalleryCacheBase {

	private final GalleryModel galleryModel;
	private final GalleryView<?, ?> galleryView;
	
	private final Object widthMode;
	private final Object heightMode;

	GalleryCacheAllProvisionalSomeComplete(
			JSInvocable runtime,
			Object jsObject,
			GalleryModel galleryModel,
			GalleryView<?, ?> galleryView,
			Object widthMode,
			Object heightMode) {
		
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
		
		if (widthMode == null) {
			throw new IllegalArgumentException("widthMode == null");
		}
		
		if (heightMode == null) {
			throw new IllegalArgumentException("heightMode == null");
		}

		this.galleryModel = galleryModel;
		this.galleryView = galleryView;
		
		this.widthMode = widthMode;
		this.heightMode = heightMode;
	}
	
	void refresh(int totalNumberOfItems, WidthMode widthMode, HeightMode heightMode) {
		
		invokeMethod("refresh", 0, totalNumberOfItems, widthMode, heightMode);
		
	}
	
	/**
	 * To be passed in if are JS instances
	 * @param totalNumberOfItems
	 * @param widthMode
	 * @param heightMode
	 */

	void refreshWithJSObjs(int totalNumberOfItems) {
		
		invokeMethod("refresh", 0, totalNumberOfItems, widthMode, heightMode);
		
	}
	
	public Object getWidthMode() {
		return widthMode;
	}

	public Object getHeightMode() {
		return heightMode;
	}
}

