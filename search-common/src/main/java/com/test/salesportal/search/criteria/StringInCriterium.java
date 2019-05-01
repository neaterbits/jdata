package com.test.salesportal.search.criteria;

import java.util.List;

import com.test.salesportal.model.ItemAttribute;

public class StringInCriterium extends InCriterium<String> {

	public StringInCriterium(ItemAttribute attribute, List<InCriteriumValue<String>> values, boolean includeItemsWithNoValue) {
		super(attribute, values, includeItemsWithNoValue);
	}
}
