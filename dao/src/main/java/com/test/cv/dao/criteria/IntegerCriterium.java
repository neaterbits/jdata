package com.test.cv.dao.criteria;

import com.test.cv.model.ItemAttribute;

public class IntegerCriterium extends SingleValueCriteria<Integer> {

	public IntegerCriterium(ItemAttribute attribute, int value, ComparisonOperator comparisonOperator) {
		super(attribute, value, comparisonOperator);
	}
}
