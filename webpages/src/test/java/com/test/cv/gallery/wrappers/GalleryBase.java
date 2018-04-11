package com.test.cv.gallery.wrappers;

import com.test.cv.jsutils.JSInvocable;
import com.test.cv.jsutils.JavaWrapper;

public class GalleryBase extends JavaWrapper {

	public GalleryBase(JSInvocable invocable, Object jsObject) {
		super(invocable, jsObject);
	}
	
	public <T> T [] scrollVirtualArrayView(
			Class<T> arrayMemberClass,
			T [] array,
			int prevFirstViewIndex, int prevLastViewIndex,
			int firstOverlapCheckIndex, int lastOverlapCheckIndex,
			int newArrayFirstViewIndex, int newArrayLastViewIndex,
			T defaultValue) {
		
		
		final Object jsObj = this.invokeMethod(
				"scrollVirtualArrayView",
				0, // level
				array,
				prevFirstViewIndex, prevLastViewIndex,
				firstOverlapCheckIndex, lastOverlapCheckIndex,
				newArrayFirstViewIndex, newArrayLastViewIndex,
				defaultValue);
		
		return getJSArray(jsObj, arrayMemberClass);
	}
}
