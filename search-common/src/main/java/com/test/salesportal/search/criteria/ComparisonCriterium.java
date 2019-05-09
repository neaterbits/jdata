package com.test.salesportal.search.criteria;

import com.test.salesportal.model.ItemAttribute;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((comparisonOperator == null) ? 0 : comparisonOperator.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComparisonCriterium<?> other = (ComparisonCriterium<?>) obj;
		if (comparisonOperator != other.comparisonOperator)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [value=" + value + ", comparisonOperator=" + comparisonOperator
				+ ", getAttribute()=" + getAttribute() + "]";
	}
}
