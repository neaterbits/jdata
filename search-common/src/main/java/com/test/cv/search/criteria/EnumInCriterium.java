package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public class EnumInCriterium<E extends Enum<E>> extends InCriterium<E> {

	public EnumInCriterium(ItemAttribute attribute, E [] values, boolean includeItemsWithNoValue) {
		super(attribute, values, includeItemsWithNoValue);
	}
}

