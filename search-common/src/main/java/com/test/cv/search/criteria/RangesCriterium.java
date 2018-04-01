package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public abstract class RangesCriterium<T extends Comparable<T>, R extends Range<T>> extends Criterium {

	private final R [] ranges;
	private final boolean includeItemsWithNoValue;

	RangesCriterium(ItemAttribute attribute, R [] ranges, boolean includeItemsWithNoValue) {
		super(attribute);

		this.ranges = ranges;
		this.includeItemsWithNoValue = includeItemsWithNoValue;
	}

	public final R[] getRanges() {
		return ranges;
	}

	public boolean includeItemsWithNoValue() {
		return includeItemsWithNoValue;
	}
}
