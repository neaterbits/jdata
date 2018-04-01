package com.test.cv.search.criteria;

import java.util.List;

import com.test.cv.model.ItemAttribute;

public class StringInCriterium extends InCriterium<String> {

	public StringInCriterium(ItemAttribute attribute, List<InCriteriumValue<String>> values, boolean includeItemsWithNoValue) {
		super(attribute, values, includeItemsWithNoValue);
	}
}
