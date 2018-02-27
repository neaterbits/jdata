package com.test.cv.search.criteria;

import com.test.cv.model.ItemAttribute;

public class RangeCriteria<T> extends Criterium {

	private final T lowerValue;
	private final boolean includeLower;
	private final T upperValue;
	private final boolean includeUpper;
	
	RangeCriteria(ItemAttribute attribute, T lowerValue, boolean includeLower, T upperValue,
			boolean includeUpper) {
		super(attribute);
		this.lowerValue = lowerValue;
		this.includeLower = includeLower;
		this.upperValue = upperValue;
		this.includeUpper = includeUpper;
	}

	public T getLowerValue() {
		return lowerValue;
	}
	
	public boolean includeLower() {
		return includeLower;
	}
	
	public T getUpperValue() {
		return upperValue;
	}

	public boolean includeUpper() {
		return includeUpper;
	}
}
