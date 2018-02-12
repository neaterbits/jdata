package com.test.cv.dao.criteria;

import java.math.BigDecimal;

import com.test.cv.model.ItemAttribute;

public class DecimalCriteria extends SingleValueCriteria<BigDecimal> {

	public DecimalCriteria(ItemAttribute attribute, BigDecimal value, ComparisonOperator comparisonOperator) {
		super(attribute, value, comparisonOperator);
	}
}
