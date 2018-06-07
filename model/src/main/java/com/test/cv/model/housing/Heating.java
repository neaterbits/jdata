package com.test.cv.model.housing;

import com.test.cv.model.AttributeEnum;

public enum Heating implements AttributeEnum {
	AVAILABLE("Available");
	
	private final String displayName;

	private Heating(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
