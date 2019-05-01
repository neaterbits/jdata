package com.test.salesportal.gallery.wrappers;

import com.test.salesportal.gallery.api.CacheItems;
import com.test.salesportal.jsutils.JSInvocable;
import com.test.salesportal.jsutils.JavaWrapper;

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

	@Override
	public void clear(int level) {
		
		invokeMethod("clear", level);
	}
}
