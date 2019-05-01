package com.test.salesportal.search.criteria;

import java.math.BigDecimal;

import com.test.salesportal.model.ItemAttribute;

public class DecimalRangesCriterium extends RangesCriterium<BigDecimal, DecimalRange> {

	public DecimalRangesCriterium(ItemAttribute attribute, DecimalRange[] ranges, boolean includeItemsWithNoValue) {
		super(attribute, ranges, includeItemsWithNoValue);
	}
}
