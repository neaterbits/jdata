package com.test.salesportal.rest.search.model.facetresult;

public abstract class SearchFacetedAttributeRangeResult<T> {

	private T lower;
	private T upper;
	private int matchCount;
	
	public SearchFacetedAttributeRangeResult() {
		
	}

	public SearchFacetedAttributeRangeResult(T lower, T upper, int matchCount) {
		this.lower = lower;
		this.upper = upper;
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

	public int getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(int matchCount) {
		this.matchCount = matchCount;
	}
}
