package com.test.salesportal.search.criteria;

import java.util.List;

import com.test.salesportal.model.items.ItemAttribute;

public abstract class InCriterium<T extends Comparable<T>> extends ValueCriterium<T> {

	private final List<InCriteriumValue<T>>  values;
	private final boolean includeItemsWithNoValue;

	public InCriterium(ItemAttribute attribute, List<InCriteriumValue<T>> values, boolean includeItemsWithNoValue) {
		super(attribute);
		
		this.values = values;
		this.includeItemsWithNoValue = includeItemsWithNoValue;
	}

	public final List<InCriteriumValue<T>> getValues() {
		return values;
	}

	public final boolean includeItemsWithNoValue() {
		return includeItemsWithNoValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (includeItemsWithNoValue ? 1231 : 1237);
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		InCriterium<?> other = (InCriterium<?>) obj;
		if (includeItemsWithNoValue != other.includeItemsWithNoValue)
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [values=" + values + ", includeItemsWithNoValue=" + includeItemsWithNoValue
				+ ", getAttribute()=" + getAttribute() + "]";
	}
}
