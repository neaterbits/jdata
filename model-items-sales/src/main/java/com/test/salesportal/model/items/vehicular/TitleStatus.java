package com.test.salesportal.model.items.vehicular;

import com.test.salesportal.model.items.AttributeEnum;

public enum TitleStatus implements AttributeEnum {

	CLEAN("Clean"),
	LIEN("Lien"),
	REBUILT("Rebuilt"),
	MISSING("Missing"),
	SALVAGE("Salvage"),
	PARTS_ONLY("Parts Only");
	
	private final String displayName;

	private TitleStatus(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
