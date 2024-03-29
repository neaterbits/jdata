package com.test.salesportal.gallery.api;

/**
 * Cached items used for downloading and caching items in display area
 * and possibly preload items around display area
 * for fastes scrolling close to displayed area.
 */

public interface CacheItems {
	
	void updateVisibleArea(
			int level,
			int firstVisibleIndex,
			int visibleCount,
			int totalNumberOfItems,
			Object updateVisibleAreaCompleteCallback);

	void clear(int level);
}
