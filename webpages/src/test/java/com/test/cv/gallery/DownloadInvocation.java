package com.test.cv.gallery;

import com.test.cv.jsutils.JSFunction;

public class DownloadInvocation {
	private final int startIndex;
	private final int count;
	private final JSFunction callback;
	
	DownloadInvocation(int startIndex, int count, JSFunction callback) {
		this.startIndex = startIndex;
		this.count = count;
		this.callback = callback;
	}
	
	int getStartIndex() {
		return startIndex;
	}

	int getCount() {
		return count;
	}

	void onDownloaded() {
		
		// Just return an array of strings, this would be images or thumb sizes or similar for a real gallery
		final String [] result = new String[count];
		
		for (int i = 0; i < result.length; ++ i) {
			result[i] = dataString(startIndex, count, i);
		}
		
		callback.callWithArray(result);
	}

	static final String dataString(int startIndex, int count, int arrayIndex) {
		 return "Downloaded-item at index " + (startIndex + arrayIndex) + " / " + arrayIndex + " out of " + count;
	}

	@Override
	public String toString() {
		return "[startIndex=" + startIndex + ", count=" + count +  "]";
	}
}
