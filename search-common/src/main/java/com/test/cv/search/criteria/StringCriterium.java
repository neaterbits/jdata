package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public class StringCriterium extends ComparisonCriterium<String> {

	public StringCriterium(ItemAttribute attribute, String value, ComparisonOperator comparisonOperator) {
		super(attribute, value, comparisonOperator);
	}
}
