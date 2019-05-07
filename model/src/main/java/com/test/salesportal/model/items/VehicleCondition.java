package com.test.salesportal.model.items;

import com.test.salesportal.model.AttributeEnum;

public enum VehicleCondition implements AttributeEnum {
	NEW("New"),
	LIKE_NEW("Like new"),
	EXCELLENT("Excellent"),
	GOOD("Good"),
	FAIR("Fair"),
	SALVAGE("Salvage");
	
	private final String displayName;

	private VehicleCondition(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}