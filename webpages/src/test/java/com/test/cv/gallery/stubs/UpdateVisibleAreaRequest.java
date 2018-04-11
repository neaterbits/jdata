package com.test.cv.gallery.stubs;

import com.test.cv.jsutils.JSFunction;

public class UpdateVisibleAreaRequest {
	private final int firstVisibleIndex;
	private final int visibleCount;
	private final int totalNumberOfItems;
	private final JSFunction updateVisibleAreaCompleteCallback;

	
	UpdateVisibleAreaRequest(int firstVisibleIndex, int visibleCount, int totalNumberOfItems,
			JSFunction updateVisibleAreaCompleteCallback) {
	
		if (updateVisibleAreaCompleteCallback == null) {
			throw new IllegalArgumentException("updateVisibleAreaCompleteCallback == null");
		}
		
		this.firstVisibleIndex = firstVisibleIndex;
		this.visibleCount = visibleCount;
		this.totalNumberOfItems = totalNumberOfItems;
		this.updateVisibleAreaCompleteCallback = updateVisibleAreaCompleteCallback;
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

	@Override
	public String toString() {
		return "UpdateVisibleAreaRequest [firstVisibleIndex=" + firstVisibleIndex + ", visibleCount=" + visibleCount
				+ ", totalNumberOfItems=" + totalNumberOfItems + ", updateVisibleAreaCompleteCallback="
				+ updateVisibleAreaCompleteCallback + "]";
	}
}
