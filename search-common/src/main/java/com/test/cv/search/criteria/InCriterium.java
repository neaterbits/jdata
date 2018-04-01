package com.test.cv.search.criteria;

import java.util.List;

import com.test.cv.model.ItemAttribute;

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
}
