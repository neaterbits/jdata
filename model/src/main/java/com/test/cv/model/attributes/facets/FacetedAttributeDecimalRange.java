package com.test.cv.model.attributes.facets;

import java.math.BigDecimal;

public final class FacetedAttributeDecimalRange {

	private final BigDecimal lower;
	private final BigDecimal upper;

	public FacetedAttributeDecimalRange(BigDecimal lower, BigDecimal upper) {
		this.lower = lower;
		this.upper = upper;
	}

	public BigDecimal getLower() {
		return lower;
	}

	public BigDecimal getUpper() {
		return upper;
	}
}
