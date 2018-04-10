package com.test.cv.gallery.stubs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.test.cv.gallery.api.GalleryModel;
import com.test.cv.jsutils.JSFunction;

public class GalleryModelStub implements GalleryModel {
	
	private final List<DownloadInvocation> downloadProvisional  = new ArrayList<>();
	private final List<DownloadInvocation> downloadComplete = new ArrayList<>();

	private final Function<Object, JSFunction> getJSFunction;
	private final MakeDownloadData makeDownloadData;

	public GalleryModelStub(Function<Object, JSFunction> getJSFunction, MakeDownloadData makeDownloadData) {
		if (getJSFunction == null) {
			throw new IllegalArgumentException("getJSFunction == null");
		}

		this.getJSFunction = getJSFunction;
		this.makeDownloadData = makeDownloadData;
	}
	
	public int getProvisionalRequestCount() {
		return downloadProvisional.size();
	}
	
	public DownloadInvocation getProvisionalRequestAt(int index) {
		return downloadProvisional.get(index);
	}
	
	public void clearProvisionalRequests() {
		downloadProvisional.clear();
	}
	
	public int getCompleteRequestCount() {
		return downloadComplete.size();
	}

	@Override
	public void getProvisionalData(int index, int count, Object onSuccess) {
		downloadProvisional.add(new DownloadInvocation(index, count, getJSFunction.apply(onSuccess), makeDownloadData));
	}

	@Override
	public void getCompleteData(int index, int count, Object onSuccess) {
		downloadComplete.add(new DownloadInvocation(index, count, getJSFunction.apply(onSuccess), makeDownloadData));
	}
}

