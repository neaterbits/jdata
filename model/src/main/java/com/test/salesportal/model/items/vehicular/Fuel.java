package com.test.salesportal.model.items.vehicular;

import com.test.salesportal.model.AttributeEnum;

public enum Fuel implements AttributeEnum {
	GAS("Gas"),
	DIESEL("Diesel"),
	HYBRID("Hybrid"),
	ELECTRIC("Electric");

	private final String displayName;

	private Fuel(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
