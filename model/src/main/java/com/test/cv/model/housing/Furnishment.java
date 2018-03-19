package com.test.cv.model.housing;

import com.test.cv.model.AttributeEnum;

public enum Furnishment implements AttributeEnum {
	FURNISHED("Furnished"),
	PARTLY_FURNISHED("Partly furnished"),
	NOT_FURNISHED("Not furnished");
	
	private final String displayName;

	private Furnishment(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
