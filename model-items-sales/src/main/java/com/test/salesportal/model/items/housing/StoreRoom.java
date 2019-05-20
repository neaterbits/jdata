package com.test.salesportal.model.items.housing;

import com.test.salesportal.model.items.AttributeEnum;

public enum StoreRoom implements AttributeEnum {
	AVAILABLE("Available");

	private final String displayName;
	
	private StoreRoom(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
