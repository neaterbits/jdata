package com.test.cv.gallery;

import com.test.cv.jsutils.JSInvocable;
import com.test.cv.jsutils.JavaWrapper;

/**
 * Java wrapper class for JS class for API typesafety and readability
 */

final class GalleryCacheItems extends JavaWrapper {

	GalleryCacheItems(JSInvocable invocable, Object jsObject) {
		super(invocable, jsObject);
	}

	void updateVisibleArea(
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
