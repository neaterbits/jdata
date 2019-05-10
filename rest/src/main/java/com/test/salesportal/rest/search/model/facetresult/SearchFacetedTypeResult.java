package com.test.salesportal.rest.search.model.facetresult;

import java.util.List;

/**
 * Search facet results for all facets for all types available
 */
public class SearchFacetedTypeResult {

	private String type;
	private String displayName;
	private Integer autoExpandAttributesCount;
	
	public SearchFacetedTypeResult() {

	}

	public SearchFacetedTypeResult(String type, String displayName) {
		this.type = type;
		this.displayName = displayName;
	}

	private List<SearchFacetedTypeResult> subTypes;
	
	private List<SearchFacetedAttributeResult> attributes;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public Integer getAutoExpandAttributesCount() {
		return autoExpandAttributesCount;
	}

	public void setAutoExpandAttributesCount(Integer autoExpandAttributesCount) {
		this.autoExpandAttributesCount = autoExpandAttributesCount;
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
