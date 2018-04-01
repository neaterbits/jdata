package com.test.cv.search.criteria;

import java.util.List;

import com.test.cv.model.ItemAttribute;

public class EnumInCriterium<E extends Enum<E>> extends InCriterium<E> {

	public EnumInCriterium(ItemAttribute attribute, List<InCriteriumValue<E>> values, boolean includeItemsWithNoValue) {
		super(attribute, values, includeItemsWithNoValue);
	}
}

