package com.test.salesportal.model.housing;

import com.test.salesportal.model.AttributeEnum;

public enum CostPeriod implements AttributeEnum {
	DAILY("Daily"),
	MONTHLY("Monthly"),
	YEARLY("Yearly");
	
	private final String displayName;

	private CostPeriod(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}
