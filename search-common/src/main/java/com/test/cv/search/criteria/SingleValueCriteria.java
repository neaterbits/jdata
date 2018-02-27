package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public abstract class SingleValueCriteria<T> extends Criterium {

	private final T value;
	private final ComparisonOperator comparisonOperator;

	public SingleValueCriteria(ItemAttribute attribute, T value, ComparisonOperator comparisonOperator) {
		super(attribute);
		
		this.value = value;
		this.comparisonOperator = comparisonOperator;
	}

	public final T getValue() {
		return value;
	}

	public final ComparisonOperator getComparisonOperator() {
		return comparisonOperator;
	}
}
