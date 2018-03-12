package com.test.cv.rest;

import java.util.List;

/**
 * Search facet results for all facets for all types available
 */
public class SearchFacetedTypeResult {

	private String type;
	private String typeDisplayName;
	
	private List<SearchFacetedTypeResult> subTypes;
	
	private List<SearchFacetedAttributeResult> attributes;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeDisplayName() {
		return typeDisplayName;
	}

	public void setTypeDisplayName(String typeDisplayName) {
		this.typeDisplayName = typeDisplayName;
	}
	
	public List<SearchFacetedTypeResult> getSubTypes() {
		return subTypes;
	}

	public void setSubTypes(List<SearchFacetedTypeResult> subTypes) {
		this.subTypes = subTypes;
	}

	public List<SearchFacetedAttributeResult> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<SearchFacetedAttributeResult> attributes) {
		this.attributes = attributes;
	}
}
