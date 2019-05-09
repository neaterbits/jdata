package com.test.salesportal.rest.search.model.facetresult;

import java.util.Arrays;
import java.util.List;

public class SearchSingleValueFacet {
	
	private Object value;
	
	// If what to display differs from value, null otherwise
	private Object displayValue;
	
	// Number matching this attribute
	private int matchCount;

	// eg item type is sub attribute of item category (snowboard under sports)
	// or county is sub attribute of state (for apartments and houses)
	private List<SearchFacetedAttributeResult> subAttributes;

	public SearchSingleValueFacet() {
		
	}
	
	public SearchSingleValueFacet(Object value, int matchCount) {
		this(value, matchCount, null, true);
	}

	public SearchSingleValueFacet(Object value, int matchCount, SearchFacetedAttributeResult ... subAttributes) {
		this(
			value,
			matchCount,
			subAttributes != null
				? subAttributes.length == 0 ? null : Arrays.asList(subAttributes)
				: null,
			true);
	}
	
	private SearchSingleValueFacet(Object value, int matchCount, List<SearchFacetedAttributeResult> subAttributes, boolean disambiguate) {
		if (value == null) {
			throw new IllegalArgumentException("value == null");
		}
		
		this.value = value;
		this.matchCount = matchCount;
		this.subAttributes = subAttributes;
	}
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public Object getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(Object displayValue) {
		this.displayValue = displayValue;
	}

	public int getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(int matchCount) {
		this.matchCount = matchCount;
	}

	public List<SearchFacetedAttributeResult> getSubAttributes() {
		return subAttributes;
	}

	public void setSubAttributes(List<SearchFacetedAttributeResult> subAttributes) {
		this.subAttributes = subAttributes;
	}

	@Override
	public String toString() {
		return "SearchSingleValueFacet [value=" + value + ", displayValue=" + displayValue + ", matchCount="
				+ matchCount + ", subAttributes=" + subAttributes + "]";
	}
}
