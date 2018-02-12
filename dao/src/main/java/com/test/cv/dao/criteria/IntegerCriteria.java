package com.test.cv.dao.criteria;

import com.test.cv.model.ItemAttribute;

public class IntegerCriteria extends SingleValueCriteria<Integer> {

	public IntegerCriteria(ItemAttribute attribute, int value, ComparisonOperator comparisonOperator) {
		super(attribute, value, comparisonOperator);
	}
}
