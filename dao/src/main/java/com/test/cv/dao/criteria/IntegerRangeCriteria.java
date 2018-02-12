package com.test.cv.dao.criteria;

import com.test.cv.model.ItemAttribute;

public class IntegerRangeCriteria extends RangeCriteria<Integer> {

	public IntegerRangeCriteria(ItemAttribute attribute, int lowerValue, boolean includeLower, int upperValue,
			boolean includeUpper) {
		super(attribute, lowerValue, includeLower, upperValue, includeUpper);
	}
}
