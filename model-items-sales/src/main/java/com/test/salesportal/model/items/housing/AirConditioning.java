package com.test.salesportal.model.items.housing;

import com.test.salesportal.model.items.AttributeEnum;

public enum AirConditioning implements AttributeEnum {
	AVAILABLE("Available");
	
	private final String displayName;

	private AirConditioning(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
