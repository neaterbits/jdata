package com.test.cv.rest;

import java.util.List;

/**
 * Search facet results for all facets for all types available
 */
public class FacetResult {

	private String type;
	private String typeDisplayName;
	private List<FacetAttribute> attributes;

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

	public List<FacetAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<FacetAttribute> attributes) {
		this.attributes = attributes;
	}
}
