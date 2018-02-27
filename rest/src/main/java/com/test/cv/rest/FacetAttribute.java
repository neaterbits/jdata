package com.test.cv.rest;

import java.util.List;

public class FacetAttribute {
	
	// Id of attribute for further reference
	private String id;
	
	// Readable name to display in UI
	private String name;
	
	// Number matching this attribute
	private int matchCount;

	// eg item type is sub attribute of item category (snowboard under sports)
	// or county is sub attribute of state (for apartments and houses)
	private List<FacetAttribute> subAttributes;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(int matchCount) {
		this.matchCount = matchCount;
	}

	public List<FacetAttribute> getSubAttributes() {
		return subAttributes;
	}

	public void setSubAttributes(List<FacetAttribute> subAttributes) {
		this.subAttributes = subAttributes;
	}
}
