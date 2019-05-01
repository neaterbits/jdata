package com.test.salesportal.model.housing;

import com.test.salesportal.model.AttributeEnum;

public enum ApartmentRenter implements AttributeEnum {
	AGENT("Agent"),
	PRIVATE("Private");
	
	private final String displayName;

	private ApartmentRenter(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
