package com.test.cv.search.criteria;

import java.math.BigDecimal;

import com.test.cv.model.ItemAttribute;

public class DecimalRangesCriterium extends RangesCriterium<BigDecimal, DecimalRange> {

	public DecimalRangesCriterium(ItemAttribute attribute, DecimalRange[] ranges) {
		super(attribute, ranges);
	}
}
