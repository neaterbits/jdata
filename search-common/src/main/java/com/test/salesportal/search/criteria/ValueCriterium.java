package com.test.salesportal.search.criteria;

import com.test.salesportal.model.items.ItemAttribute;

public abstract class ValueCriterium<T> extends Criterium {

	public ValueCriterium(ItemAttribute attribute) {
		super(attribute);
	}
}
