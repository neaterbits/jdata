package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public class StringInCriterium extends InCriterium<String> {

	public StringInCriterium(ItemAttribute attribute, String[] values) {
		super(attribute, values);
	}
}
