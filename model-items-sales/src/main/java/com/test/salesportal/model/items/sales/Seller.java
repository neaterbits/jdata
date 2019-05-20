package com.test.salesportal.model.items.sales;

import com.test.salesportal.model.items.AttributeEnum;

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
