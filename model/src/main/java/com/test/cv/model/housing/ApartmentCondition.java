package com.test.cv.model.housing;

import com.test.cv.model.AttributeEnum;

public enum ApartmentCondition implements AttributeEnum {

	NEWLY_RENOVATED("Newly renovated"),
	EUROSTANDARD("Eurostandard"),
	OLD_RENOVATED("Old renovated"),
	RENOVATED("Renovated"),
	UNDER_RENOVATION("Under renovation"),
	W_O_RENOVATION("W/o renovation"),
	WHITE_FRAME("White frame"),
	BLACK_FRAME("Black frame");

	private final String displayName;

	private ApartmentCondition(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
