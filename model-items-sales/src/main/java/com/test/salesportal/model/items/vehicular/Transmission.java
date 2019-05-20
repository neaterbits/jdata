package com.test.salesportal.model.items.vehicular;

import com.test.salesportal.model.items.AttributeEnum;

public enum Transmission implements AttributeEnum {

	AUTOMATIC("Automatic"),
	MANUAL("Manual");
	
	private final String displayName;

	private Transmission(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
