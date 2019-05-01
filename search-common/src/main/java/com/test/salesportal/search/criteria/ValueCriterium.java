package com.test.salesportal.search.criteria;

import com.test.salesportal.model.ItemAttribute;

public abstract class ValueCriterium<T> extends Criterium {

	public ValueCriterium(ItemAttribute attribute) {
		super(attribute);
	}
}
