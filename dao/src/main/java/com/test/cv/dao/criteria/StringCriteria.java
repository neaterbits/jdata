package com.test.cv.dao.criteria;

import com.test.cv.model.ItemAttribute;

public class StringCriteria extends SingleValueCriteria<String> {

	public StringCriteria(ItemAttribute attribute, String value, ComparisonOperator comparisonOperator) {
		super(attribute, value, comparisonOperator);
	}
}
