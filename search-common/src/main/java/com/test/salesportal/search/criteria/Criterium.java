package com.test.salesportal.search.criteria;

import com.test.salesportal.model.ItemAttribute;

public abstract class Criterium {
	private final ItemAttribute attribute;

	public Criterium(ItemAttribute attribute) {

		if (attribute == null) {
			throw new IllegalArgumentException("property == null");
		}
		
		this.attribute = attribute;
	}

	public final ItemAttribute getAttribute() {
		return attribute;
	}
}
