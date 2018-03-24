package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public abstract class InCriterium<T extends Comparable<T>> extends ValueCriterium<T> {

	private final T [] values;
	private final boolean includeItemsWithNoValue;

	public InCriterium(ItemAttribute attribute, T [] values, boolean includeItemsWithNoValue) {
		super(attribute);
		
		this.values = values;
		this.includeItemsWithNoValue = includeItemsWithNoValue;
	}

	public final T[] getValues() {
		return values;
	}

	public final boolean includeItemsWithNoValue() {
		return includeItemsWithNoValue;
	}
}
