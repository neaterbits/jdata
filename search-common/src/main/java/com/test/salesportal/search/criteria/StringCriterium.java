package com.test.salesportal.search.criteria;

import com.test.salesportal.model.ItemAttribute;

public class StringCriterium extends ComparisonCriterium<String> {

	public StringCriterium(ItemAttribute attribute, String value, ComparisonOperator comparisonOperator) {
		super(attribute, value, comparisonOperator);
	}
}
