package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public class IntegerInCriterium extends InCriterium<Integer> {

	public IntegerInCriterium(ItemAttribute attribute, Integer[] values, boolean includeItemsWithNoValue) {
		super(attribute, values, includeItemsWithNoValue);
	}
}
