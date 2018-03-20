package com.test.cv.model.items.vehicular;

import com.test.cv.model.AttributeEnum;

public enum Fuel implements AttributeEnum {
	GAS("Gas"),
	DIESEL("Diesel");

	private final String displayName;

	private Fuel(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
