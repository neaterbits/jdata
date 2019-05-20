package com.test.salesportal.model.items.housing;

import com.test.salesportal.model.items.AttributeEnum;

public enum BuildingStatus implements AttributeEnum {
	NEW_BUILDING("New building"),
	OLD_BUILDING("Old building"),
	UNDER_CONSTRUCTION("Under construction");
	
	private final String displayName;

	private BuildingStatus(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
