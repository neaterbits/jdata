package com.test.salesportal.model.attributes.facets;

public abstract class FacetedAttributeComparableRange<T extends Comparable<T>> {
	private final T lower;
	private final T upper;
	
	public FacetedAttributeComparableRange(T lower, T upper) {
		this.lower = lower;
		this.upper = upper;
	}

	public final T getLower() {
		return lower;
	}

	public final T getUpper() {
		return upper;
	}
}
