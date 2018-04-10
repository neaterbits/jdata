package com.test.cv.gallery.wrappers;

import com.test.cv.gallery.api.CacheItems;
import com.test.cv.jsutils.JSInvocable;
import com.test.cv.jsutils.JavaWrapper;

/**
 * Java wrapper class for JS class for API typesafety and readability
 */

public final class GalleryCacheItems extends JavaWrapper implements CacheItems {

	public GalleryCacheItems(JSInvocable invocable, Object jsObject) {
		super(invocable, jsObject);
	}

	public void updateVisibleArea(
			int firstVisibleIndex,
			int visibleCount,
			int totalNumberOfItems,
			Object updateVisibleAreaCompleteCallback) {

		this.updateVisibleArea(0, firstVisibleIndex, visibleCount, totalNumberOfItems, updateVisibleAreaCompleteCallback);
	}

	@Override
	public void updateVisibleArea(
			int level,
			int firstVisibleIndex,
			int visibleCount,
			int totalNumberOfItems,
			Object updateVisibleAreaCompleteCallback) {

		invokeMethod("updateVisibleArea",
				level, // debug indent level
				firstVisibleIndex,
				visibleCount,
				totalNumberOfItems,
				updateVisibleAreaCompleteCallback);
	}

}
