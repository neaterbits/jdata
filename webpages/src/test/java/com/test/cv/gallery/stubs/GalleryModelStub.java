package com.test.cv.gallery.stubs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.test.cv.gallery.api.GalleryModel;
import com.test.cv.jsutils.JSFunction;

public class GalleryModelStub implements GalleryModel {
	
	public final List<DownloadInvocation> downloadProvisional  = new ArrayList<>();
	public final List<DownloadInvocation> downloadComplete = new ArrayList<>();

	private final Function<Object, JSFunction> getJSFunction;

	public GalleryModelStub(Function<Object, JSFunction> getJSFunction) {
		if (getJSFunction == null) {
			throw new IllegalArgumentException("getJSFunction == null");
		}

		this.getJSFunction = getJSFunction;
	}
	

	@Override
	public void getProvisionalData(int index, int count, Object onSuccess) {
		downloadProvisional.add(new DownloadInvocation(index, count, getJSFunction.apply(onSuccess)));
	}

	@Override
	public void getCompleteData(int index, int count, Object onSuccess) {
		downloadComplete.add(new DownloadInvocation(index, count, getJSFunction.apply(onSuccess)));
	}

}
