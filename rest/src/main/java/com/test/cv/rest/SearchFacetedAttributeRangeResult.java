package com.test.cv.rest;

public abstract class SearchFacetedAttributeRangeResult<T> {

	private int matchCount;
	private T lower;
	private T upper;


	public int getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(int matchCount) {
		this.matchCount = matchCount;
	}

	public T getLower() {
		return lower;
	}

	public void setLower(T lower) {
		this.lower = lower;
	}

	public T getUpper() {
		return upper;
	}

	public void setUpper(T upper) {
		this.upper = upper;
	}
}
