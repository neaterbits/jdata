package com.test.salesportal.search.criteria;

import java.math.BigDecimal;

import com.test.salesportal.model.ItemAttribute;

public class DecimalCriterium extends ComparisonCriterium<BigDecimal> {

	public DecimalCriterium(ItemAttribute attribute, BigDecimal value, ComparisonOperator comparisonOperator) {
		super(attribute, value, comparisonOperator);
	}
}
