package com.test.cv.search.criteria;

public abstract class Range<T extends Comparable<T>> {
	private final T lowerValue;
	private final boolean includeLower;
	private final T upperValue;
	private final boolean includeUpper;
	
	Range(T lowerValue, boolean includeLower, T upperValue, boolean includeUpper) {
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
