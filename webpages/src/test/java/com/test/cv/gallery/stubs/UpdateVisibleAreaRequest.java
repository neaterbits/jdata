package com.test.cv.gallery.stubs;

import com.test.cv.jsutils.JSFunction;

public class UpdateVisibleAreaRequest {
	private final int firstVisibleIndex;
	private final int visibleCount;
	private final int totalNumberOfItems;
	private final JSFunction updateVisibleAreaCompleteCallback;
	private final MakeDownloadData makeDownloadData;
	
	
	UpdateVisibleAreaRequest(int firstVisibleIndex, int visibleCount, int totalNumberOfItems,
			JSFunction updateVisibleAreaCompleteCallback, MakeDownloadData makeDownloadData) {
	
		if (updateVisibleAreaCompleteCallback == null) {
			throw new IllegalArgumentException("updateVisibleAreaCompleteCallback == null");
		}

		if (makeDownloadData == null) {
			throw new IllegalArgumentException("makeDownloadData == null");
		}

		this.firstVisibleIndex = firstVisibleIndex;
		this.visibleCount = visibleCount;
		this.totalNumberOfItems = totalNumberOfItems;
		this.updateVisibleAreaCompleteCallback = updateVisibleAreaCompleteCallback;
		this.makeDownloadData = makeDownloadData;
	}

	public int getFirstVisibleIndex() {
		return firstVisibleIndex;
	}

	public int getVisibleCount() {
		return visibleCount;
	}

	public int getTotalNumberOfItems() {
		return totalNumberOfItems;
	}

	public JSFunction getUpdateVisibleAreaCompleteCallback() {
		return updateVisibleAreaCompleteCallback;
	}

	public void onComplete() {
		final Object [] data = new Object[visibleCount];
		
		for (int i = 0; i < data.length; ++ i) {
			data[i] = makeDownloadData.makeDownloadData(firstVisibleIndex, visibleCount, i);
		}

		updateVisibleAreaCompleteCallback.call(firstVisibleIndex, visibleCount, data);
	}

	@Override
	public String toString() {
		return "UpdateVisibleAreaRequest [firstVisibleIndex=" + firstVisibleIndex + ", visibleCount=" + visibleCount
				+ ", totalNumberOfItems=" + totalNumberOfItems + ", updateVisibleAreaCompleteCallback="
				+ updateVisibleAreaCompleteCallback + "]";
	}
}
