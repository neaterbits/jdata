package com.test.salesportal.model.items.attributes.facets;

import java.math.BigDecimal;

public final class FacetedAttributeDecimalRange extends FacetedAttributeComparableRange<BigDecimal> {

	public FacetedAttributeDecimalRange(BigDecimal lower, BigDecimal upper) {
		super(lower, upper);
	}
}
