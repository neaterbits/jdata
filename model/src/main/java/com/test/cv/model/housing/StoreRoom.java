package com.test.cv.model.housing;

import com.test.cv.model.AttributeEnum;

public enum StoreRoom implements AttributeEnum {
	AVAILABLE("Available");

	private final String displayName;
	
	private StoreRoom(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
