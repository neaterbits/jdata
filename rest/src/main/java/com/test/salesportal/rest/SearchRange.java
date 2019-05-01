package com.test.salesportal.rest;

public class SearchRange {

	private Object lower;
	private boolean includeLower;
	private Object upper;
	private boolean includeUpper;
	
	public Object getLower() {
		return lower;
	}
	
	public void setLower(Object lower) {
		this.lower = lower;
	}
	
	
	public boolean includeLower() {
		return includeLower;
	}

	public void setIncludeLower(boolean includeLower) {
		this.includeLower = includeLower;
	}

	public Object getUpper() {
		return upper;
	}

	public void setUpper(Object upper) {
		this.upper = upper;
	}

	public boolean includeUpper() {
		return includeUpper;
	}

	public void setIncludeUpper(boolean includeUpper) {
		this.includeUpper = includeUpper;
	}
}
