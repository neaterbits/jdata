package com.test.cv.model.housing;

import com.test.cv.model.AttributeEnum;

public enum ApartmentType implements AttributeEnum {

	APARTMENT("Apartment"),
	COTTAGE_CABIN("Cottage /cabin"),
	DUPLEX("Duplex"),
	CONDO("Condo"),
	TOWNHOUSE("Townhouse"),
	HOUSE("House"),
	FLAT("Flat"),
	IN_LAW("In-law");
	
	private final String displayName;

	private ApartmentType(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
