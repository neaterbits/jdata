package com.test.salesportal.search.criteria;

import com.test.salesportal.model.ItemAttribute;

public class IntegerCriterium extends ComparisonCriterium<Integer> {

	public IntegerCriterium(ItemAttribute attribute, int value, ComparisonOperator comparisonOperator) {
		super(attribute, value, comparisonOperator);
	}
}
