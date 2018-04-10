package com.test.cv.gallery.wrappers;

import com.test.cv.jsutils.JSInvocable;
import com.test.cv.jsutils.JavaWrapper;

/**
 * Java wrapper class for JS class for API typesafety and readability
 */

public final class GalleryCacheItems extends JavaWrapper {

	public GalleryCacheItems(JSInvocable invocable, Object jsObject) {
		super(invocable, jsObject);
	}

	public void updateVisibleArea(
			int firstVisibleIndex,
			int visibleCount,
			int totalNumberOfItems,
			Object updateVisibleAreaCompleteCallback) {

		invokeMethod("updateVisibleArea",
				0, // debug indent level
				firstVisibleIndex,
				visibleCount,
				totalNumberOfItems,
				updateVisibleAreaCompleteCallback);
	}

}
