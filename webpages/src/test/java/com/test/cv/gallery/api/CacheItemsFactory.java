package com.test.cv.gallery.api;

@FunctionalInterface
public interface CacheItemsFactory {
	
	@FunctionalInterface
	public interface OnItemDownloaded {
		void onDownloaded(Object [] data);
	}
	
	@FunctionalInterface
	public interface OnCacheDownload {
		void downloadItem(int index, int count, OnItemDownloaded onDownloaded);
	}
	
	/**
	 * Create cached items holder
	 * 
	 * @param cachedBeforeAndAfter elements to preload before and after visible area
	 * @param onDownloadItem called back when cache must download an item
	 * 
	 * @return CacheItems
	 */
	CacheItems createCacheItems(int cachedBeforeAndAfter, OnCacheDownload onDownloadItem);
	
}
