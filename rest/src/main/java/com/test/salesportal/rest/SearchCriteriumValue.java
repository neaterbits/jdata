package com.test.salesportal.rest;

public class SearchCriteriumValue {

	private Object value; // value: string, integer or decimal
	
	private SearchCriterium [] subCriteria; // eg. county under state

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public SearchCriterium [] getSubCriteria() {
		return subCriteria;
	}

	public void setSubCriteria(SearchCriterium [] subCriteria) {
		this.subCriteria = subCriteria;
	}
}
