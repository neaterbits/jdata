package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public abstract class RangesCriterium<T extends Comparable<T>, R extends Range<T>> extends Criterium {

	private final R [] ranges;
	
	RangesCriterium(ItemAttribute attribute, R [] ranges) {
		super(attribute);

		this.ranges = ranges;
	}

	public final R[] getRanges() {
		return ranges;
	}
}
