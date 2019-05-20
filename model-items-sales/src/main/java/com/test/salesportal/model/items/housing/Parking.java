package com.test.salesportal.model.items.housing;

import com.test.salesportal.model.items.AttributeEnum;

public enum Parking implements AttributeEnum {
	AVAILABLE("Available"),
	CARPORT("Carport"),
	DETACHED_GARAGE("Detached garage"),
	ATTACHED_GARAGE("Attached garage"),
	OFF_STREET("Off street"),
	STREET("Street parking"),
	VALET_PARKING("Valet parking"),
	NONE("None");
	
	private final String displayName;

	private Parking(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
