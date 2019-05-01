package com.test.salesportal.model.items;

import com.test.salesportal.model.AttributeEnum;

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
