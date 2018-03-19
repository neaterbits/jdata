package com.test.cv.model.items;

import com.test.cv.model.AttributeEnum;

public enum Seller implements AttributeEnum {
	PRIVATE("Private"),
	DEALER("Dealer");
	
	private final String displayName;
	
	private Seller(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
