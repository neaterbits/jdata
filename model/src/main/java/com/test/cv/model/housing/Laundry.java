package com.test.cv.model.housing;

import com.test.cv.model.AttributeEnum;

public enum Laundry implements AttributeEnum {
	WD_IN_UNIT("W/D in unit"),
	WD_HOOKUPS("W/D hookups"),
	ON_SITE("On site"),
	IN_BLDG("In building"),
	NONE("None");
	
	private final String displayName;

	private Laundry(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
