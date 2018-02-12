package com.test.cv.dao.criteria;

import java.math.BigDecimal;

import com.test.cv.model.ItemAttribute;

public class DecimalRangeCriteria extends RangeCriteria<BigDecimal> {

	public DecimalRangeCriteria(ItemAttribute attribute, BigDecimal lowerValue, boolean includeLower,
			BigDecimal upperValue, boolean includeUpper) {
		super(attribute, lowerValue, includeLower, upperValue, includeUpper);
	}
}
