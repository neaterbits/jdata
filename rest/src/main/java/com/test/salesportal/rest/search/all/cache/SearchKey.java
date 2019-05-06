package com.test.salesportal.rest.search.all.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;

public final class SearchKey {

	private final List<Class<? extends Item>> types;
	private final String freeText;
	private final List<SearchCriterium> criteria;
	private final List<SortAttribute> sortAttributes;
	private final List<ItemAttribute> fieldAttributes;
	
	public SearchKey(
			List<Class<? extends Item>> types,
			String freeText,
			SearchCriterium[] criteria,
			List<SortAttribute> sortAttributes,
			List<ItemAttribute> fieldAttributes) {
		
		this.types = Collections.unmodifiableList(new ArrayList<>(types));
		this.freeText = freeText;
		this.criteria = Collections.unmodifiableList(Arrays.asList(criteria));
		this.sortAttributes = Collections.unmodifiableList(new ArrayList<>(sortAttributes));
		this.fieldAttributes = fieldAttributes != null
				? Collections.unmodifiableList(new ArrayList<>(fieldAttributes))
				: null;
	}
	
	public List<Class<? extends Item>> getTypes() {
		return types;
	}

	public String getFreeText() {
		return freeText;
	}

	public List<SearchCriterium> getCriteria() {
		return criteria;
	}

	public List<SortAttribute> getSortAttributes() {
		return sortAttributes;
	}

	public List<ItemAttribute> getFieldAttributes() {
		return fieldAttributes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((criteria == null) ? 0 : criteria.hashCode());
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
		if (criteria == null) {
			if (other.criteria != null)
				return false;
		} else if (!criteria.equals(other.criteria))
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
