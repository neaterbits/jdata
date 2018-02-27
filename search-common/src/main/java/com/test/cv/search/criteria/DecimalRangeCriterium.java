package com.test.cv.search.criteria;

import java.math.BigDecimal;

import com.test.cv.model.ItemAttribute;

public class DecimalRangeCriterium extends RangeCriteria<BigDecimal> {

	public DecimalRangeCriterium(ItemAttribute attribute, BigDecimal lowerValue, boolean includeLower,
			BigDecimal upperValue, boolean includeUpper) {
		super(attribute, lowerValue, includeLower, upperValue, includeUpper);
	}
}
