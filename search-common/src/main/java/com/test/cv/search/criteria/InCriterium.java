package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public abstract class InCriterium<T extends Comparable<T>> extends ValueCriterium<T> {

	private final T [] values;

	public InCriterium(ItemAttribute attribute, T [] values) {
		super(attribute);
		
		this.values = values;
	}

	public final T[] getValues() {
		return values;
	}
}
