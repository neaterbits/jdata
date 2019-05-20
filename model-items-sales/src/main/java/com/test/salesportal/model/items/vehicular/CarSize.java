package com.test.salesportal.model.items.vehicular;

import com.test.salesportal.model.items.AttributeEnum;

public enum CarSize implements AttributeEnum {

	SUB_COMPACT("Sub-compact"),
	COMPACT("Compact"),
	MID_SIZE("Mid-size"),
	FULL_SIZE("Full-size");
	
	private final String displayName;

	private CarSize(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
