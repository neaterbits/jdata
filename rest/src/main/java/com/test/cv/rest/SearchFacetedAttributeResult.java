package com.test.cv.rest;

public abstract class SearchFacetedAttributeResult {
	
	// Id of attribute for further reference
	private String id;
	
	// Readable name to display in UI
	private String name;
	
	// Elements that had no attribute value that could be faceted (ie. 'Other' or 'Unknown' checkbox in UI)
	private Integer noAttributeValueCount;

	public SearchFacetedAttributeResult() {
		
	}

	public SearchFacetedAttributeResult(String id, String name, Integer noAttributeValueCount) {
		this.id = id;
		this.name = name;
		this.noAttributeValueCount = noAttributeValueCount;
	}

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

	public Integer getNoAttributeValueCount() {
		return noAttributeValueCount;
	}

	public void setNoAttributeValueCount(int noAttributeValueCount) {
		this.noAttributeValueCount = noAttributeValueCount;
	}
}
