package com.test.salesportal.search.criteria;

import java.util.List;

import com.test.salesportal.model.ItemAttribute;

public class EnumInCriterium<E extends Enum<E>> extends InCriterium<E> {

	public EnumInCriterium(ItemAttribute attribute, List<InCriteriumValue<E>> values, boolean includeItemsWithNoValue) {
		super(attribute, values, includeItemsWithNoValue);
	}
}

