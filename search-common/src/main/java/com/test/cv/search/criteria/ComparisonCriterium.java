package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public abstract class ComparisonCriterium<T extends Comparable<T>> 
	extends ValueCriterium<T> {

	private final T value;
	private final ComparisonOperator comparisonOperator;

	public ComparisonCriterium(ItemAttribute attribute, T value, ComparisonOperator comparisonOperator) {
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
