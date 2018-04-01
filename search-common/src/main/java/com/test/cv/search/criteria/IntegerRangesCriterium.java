package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public class IntegerRangesCriterium extends RangesCriterium<Integer, IntegerRange> {

	public IntegerRangesCriterium(ItemAttribute attribute, IntegerRange[] ranges, boolean includeItemsWithNoValue) {
		super(attribute, ranges, includeItemsWithNoValue);
	}
}
