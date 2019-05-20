package com.test.salesportal.model.items.housing;

import com.test.salesportal.model.items.AttributeEnum;

public enum Heating implements AttributeEnum {
	AVAILABLE("Available");
	
	private final String displayName;

	private Heating(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
