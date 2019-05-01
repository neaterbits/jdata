package com.test.salesportal.search.criteria;

import com.test.salesportal.model.ItemAttribute;

/**
 * Criterium for search matching when is no value for attribute
 * 
 */
public class NoValueCriterium extends Criterium {

	public NoValueCriterium(ItemAttribute attribute) {
		super(attribute);
	}
}
