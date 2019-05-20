package com.test.salesportal.search.criteria;

import com.test.salesportal.model.items.ItemAttribute;

public class IntegerRangesCriterium extends RangesCriterium<Integer, IntegerRange> {

	public IntegerRangesCriterium(ItemAttribute attribute, IntegerRange[] ranges, boolean includeItemsWithNoValue) {
		super(attribute, ranges, includeItemsWithNoValue);
	}
}
