package com.test.cv.rest;

public class SearchCriteriumValue {

	private Object value; // value: string, integer or decimal
	
	private SearchCriteria subCriteria; // eg. county under state

	Object getValue() {
		return value;
	}

	void setValue(Object value) {
		this.value = value;
	}

	SearchCriteria getSubCriteria() {
		return subCriteria;
	}

	void setSubCriteria(SearchCriteria subCriteria) {
		this.subCriteria = subCriteria;
	}
}
