package com.test.cv.rest;

public class SearchCriterium {
	private String type; // type of object this belongs to
	private String attribute; // attribute for thus
	
	// either a value or a range
	private Object value;
	private SearchRange range;
	
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public SearchRange getRange() {
		return range;
	}
	public void setRange(SearchRange range) {
		this.range = range;
	}
	
	
}
