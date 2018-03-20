package com.test.cv.model.items.vehicular;

import com.test.cv.model.AttributeEnum;

public enum TitleStatus implements AttributeEnum {

	CLEAN("Clean"),
	SALVAGE("Salvage");
	
	private final String displayName;

	private TitleStatus(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
