package com.test.cv.search.criteria;

import java.math.BigDecimal;

import com.test.cv.model.ItemAttribute;

public class DecimalInCriterium extends InCriterium<BigDecimal> {

	public DecimalInCriterium(ItemAttribute attribute, BigDecimal[] values, boolean includeItemsWithNoValue) {
		super(attribute, values, includeItemsWithNoValue);
	}
}
