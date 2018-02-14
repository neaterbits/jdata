package com.test.cv.rest;

public class SearchCriterium {
	private String type; // type of object this belongs to
	private String attribute;
	
	// either a value or a range
	private Object value;
	private SearchRange range;
}
