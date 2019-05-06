package com.test.salesportal.rest.search.model.criteria;

import java.util.Arrays;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(subCriteria);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchCriteriumValue other = (SearchCriteriumValue) obj;
		if (!Arrays.equals(subCriteria, other.subCriteria))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
