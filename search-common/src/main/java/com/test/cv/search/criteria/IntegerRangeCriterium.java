package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public class IntegerRangeCriterium extends RangeCriteria<Integer> {

	public IntegerRangeCriterium(ItemAttribute attribute, int lowerValue, boolean includeLower, int upperValue,
			boolean includeUpper) {
		super(attribute, lowerValue, includeLower, upperValue, includeUpper);
	}
}
