package com.test.cv.gallery.stubs.modeldata;

public class GalleryItemData {

	// Separate types for provisional and complete data so can to typechecking in view stub
	private final ProvisionalData provisionalData;
	private final CompleteData completeData;
	
	public GalleryItemData(Integer width, Integer height) {
		this.provisionalData = new ProvisionalData(width, height);
		this.completeData = new CompleteData(width, height);
	}

	public ProvisionalData getProvisionalData() {
		return provisionalData;
	}

	public CompleteData getCompleteData() {
		return completeData;
	}
}

