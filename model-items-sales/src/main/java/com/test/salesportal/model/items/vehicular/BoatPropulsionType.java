package com.test.salesportal.model.items.vehicular;

import com.test.salesportal.model.items.AttributeEnum;

public enum BoatPropulsionType implements AttributeEnum {
	SAIL("Sail"),
	POWER("Power"),
	HUMAN("Human");
	
	private BoatPropulsionType(String displayName) {
		this.displayName = displayName;
	}

	private final String displayName;

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
