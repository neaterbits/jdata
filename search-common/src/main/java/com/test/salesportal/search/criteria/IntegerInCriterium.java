package com.test.salesportal.search.criteria;

import java.util.List;

import com.test.salesportal.model.items.ItemAttribute;

public class IntegerInCriterium extends InCriterium<Integer> {

	public IntegerInCriterium(ItemAttribute attribute, List<InCriteriumValue<Integer>>  values, boolean includeItemsWithNoValue) {
		super(attribute, values, includeItemsWithNoValue);
	}
}
