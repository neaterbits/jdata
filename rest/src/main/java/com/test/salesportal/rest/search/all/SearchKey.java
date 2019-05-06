package com.test.salesportal.rest.search.all;

import java.util.Arrays;
import java.util.List;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;

final class SearchKey {

	private final List<Class<? extends Item>> types;
	private final String freeText;
	private final SearchCriterium [] criteria;
	private final List<SortAttribute> sortAttributes;
	
	public SearchKey(List<Class<? extends Item>> types, String freeText, SearchCriterium[] criteria,
			List<SortAttribute> sortAttributes) {
		this.types = types;
		this.freeText = freeText;
		this.criteria = criteria;
		this.sortAttributes = sortAttributes;
	}
	
	public List<Class<? extends Item>> getTypes() {
		return types;
	}

	public String getFreeText() {
		return freeText;
	}

	public SearchCriterium[] getCriteria() {
		return criteria;
	}

	public List<SortAttribute> getSortAttributes() {
		return sortAttributes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(criteria);
		result = prime * result + ((freeText == null) ? 0 : freeText.hashCode());
		result = prime * result + ((sortAttributes == null) ? 0 : sortAttributes.hashCode());
		result = prime * result + ((types == null) ? 0 : types.hashCode());
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
		SearchKey other = (SearchKey) obj;
		if (!Arrays.equals(criteria, other.criteria))
			return false;
		if (freeText == null) {
			if (other.freeText != null)
				return false;
		} else if (!freeText.equals(other.freeText))
			return false;
		if (sortAttributes == null) {
			if (other.sortAttributes != null)
				return false;
		} else if (!sortAttributes.equals(other.sortAttributes))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}
}
