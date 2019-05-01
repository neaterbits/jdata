package com.test.salesportal.gallery.stubs;

import com.test.salesportal.jsutils.JSFunction;

public class DownloadInvocation {
	private final int startIndex;
	private final int count;
	private final JSFunction callback;
	private final MakeDownloadData makeDownloadData;

	public DownloadInvocation(int startIndex, int count, JSFunction callback, MakeDownloadData makeDownloadData) {
		this.startIndex = startIndex;
		this.count = count;
		this.callback = callback;
		
		if (makeDownloadData == null) {
			throw new IllegalArgumentException("makeDownloadData == null");
		}

		this.makeDownloadData = makeDownloadData;
	}

	public DownloadInvocation(int startIndex, int count, JSFunction callback) {
		this(startIndex, count, callback, (si, c, index) -> dataString(si, c, index));
	}
	
	public int getStartIndex() {
		return startIndex;
	}

	public int getCount() {
		return count;
	}

	public void onDownloaded() {
		
		// Just return an array of strings, this would be images or thumb sizes or similar for a real gallery
		final Object [] result = new Object[count];
		
		for (int i = 0; i < result.length; ++ i) {
			result[i] = makeDownloadData.makeDownloadData(startIndex, count, i);
		}
		
		callback.callWithArray(result);
	}

	public static final String dataString(int startIndex, int count, int arrayIndex) {
		 return "Downloaded-item at index " + (startIndex + arrayIndex) + " / " + arrayIndex + " out of " + count;
	}

	@Override
	public String toString() {
		return "[startIndex=" + startIndex + ", count=" + count +  "]";
	}
}
