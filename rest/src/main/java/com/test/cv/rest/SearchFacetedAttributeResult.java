package com.test.cv.rest;

public abstract class SearchFacetedAttributeResult {
	
	// Id of attribute for further reference
	private String id;
	
	// Readable name to display in UI
	private String name;
	

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
}
