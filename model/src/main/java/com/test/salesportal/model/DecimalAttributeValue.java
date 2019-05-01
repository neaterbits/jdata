package com.test.salesportal.model;

import java.math.BigDecimal;

public class DecimalAttributeValue extends ItemAttributeValue<BigDecimal> {

	public DecimalAttributeValue(ItemAttribute attribute, BigDecimal value) {
		super(attribute, value);
	}
}
