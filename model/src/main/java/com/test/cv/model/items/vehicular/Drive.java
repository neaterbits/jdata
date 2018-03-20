package com.test.cv.model.items.vehicular;

import com.test.cv.model.AttributeEnum;

public enum Drive implements AttributeEnum {
	FWD("FWD"),
	RWD("RWD"),
	_4WD("4WD");
	
	private final String displayName;

	private Drive(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
