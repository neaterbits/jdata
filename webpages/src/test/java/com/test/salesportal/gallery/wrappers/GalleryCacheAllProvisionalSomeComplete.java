package com.test.salesportal.gallery.wrappers;

import com.test.salesportal.gallery.api.GalleryModel;
import com.test.salesportal.gallery.api.GalleryView;
import com.test.salesportal.gallery.stubs.DisplayState;
import com.test.salesportal.jsutils.JSInvocable;

/**
 * Wraps the JS instance
 */
public class GalleryCacheAllProvisionalSomeComplete extends GalleryCacheBase {

	private final GalleryModel galleryModel;
	private final GalleryView<?, ?, ?, ?, ?, ?, ?, ?> galleryView;

	public GalleryCacheAllProvisionalSomeComplete(
			JSInvocable runtime,
			Object jsObject,
			GalleryModel galleryModel,
			GalleryView<?, ?, ?, ?, ?, ?, ?, ?> galleryView) {
		
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
	
	public void refresh(int totalNumberOfItems) {
		
		invokeMethod("refresh", 0, totalNumberOfItems);
		
	}
	
	/**
	 * To be passed in if are JS instances
	 * @param totalNumberOfItems
	 * @param widthMode
	 * @param heightMode
	 */

	public void refreshWithJSObjs(int totalNumberOfItems) {
		
		invokeMethod("refresh", 0, totalNumberOfItems);
		
	}

	public void updateOnScroll(int yPos) {
		updateOnScroll(0, yPos);
	}

	public void updateOnScroll(int level, int yPos) {
		invokeMethod("updateOnScroll", level, yPos);
	}
	
	public DisplayState whiteboxGetDisplayState() {
		final Object jsObj = invokeMethod("whiteboxGetDisplayState");

		return new DisplayState(getInvocable(), jsObj);
	}
}

