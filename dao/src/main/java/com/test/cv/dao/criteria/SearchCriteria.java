package com.test.cv.dao.criteria;

import com.test.cv.model.ItemAttribute;

public abstract class SearchCriteria {
	private final ItemAttribute attribute;

	public SearchCriteria(ItemAttribute attribute) {

		if (attribute == null) {
			throw new IllegalArgumentException("property == null");
		}
		
		this.attribute = attribute;
	}

	public final ItemAttribute getAttribute() {
		return attribute;
	}
}
