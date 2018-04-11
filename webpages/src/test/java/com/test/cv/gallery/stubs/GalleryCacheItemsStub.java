package com.test.cv.gallery.stubs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.test.cv.gallery.api.CacheItems;
import com.test.cv.jsutils.JSFunction;

public class GalleryCacheItemsStub implements CacheItems {

	private final Function<Object, JSFunction> getJSFunction;
	private final List<UpdateVisibleAreaRequest> updateRequests;
	
	public GalleryCacheItemsStub(Function<Object, JSFunction> getJSFunction) {
		if (getJSFunction == null) {
			throw new IllegalArgumentException("getJSFunction == null");
		}

		this.getJSFunction = getJSFunction;
		this.updateRequests = new ArrayList<>();
	}

	@Override
	public void updateVisibleArea(
			int level,
			int firstVisibleIndex,
			int visibleCount,
			int totalNumberOfItems,
			Object updateVisibleAreaCompleteCallback) {

		final UpdateVisibleAreaRequest request =
				new UpdateVisibleAreaRequest(
						firstVisibleIndex,
						visibleCount,
						totalNumberOfItems,
						getJSFunction.apply(updateVisibleAreaCompleteCallback));

		this.updateRequests.add(request);
	}
	
	public int getUpdateRequestCount() {
		return this.updateRequests.size();
	}

	public UpdateVisibleAreaRequest getRequestAt(int index) {
		return this.updateRequests.get(index);
	}

	public void clearUpdateRequests() {
		this.updateRequests.clear();
	}
}
