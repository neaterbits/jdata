package com.test.cv.model.items.vehicular;

import com.test.cv.model.AttributeEnum;

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
