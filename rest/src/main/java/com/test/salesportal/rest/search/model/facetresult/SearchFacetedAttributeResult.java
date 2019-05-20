package com.test.salesportal.rest.search.model.facetresult;

import com.test.salesportal.model.items.FacetFiltering;

public abstract class SearchFacetedAttributeResult {
	
	// Id of attribute for further reference
	private String id;
	
	// Readable name to display in UI
	private String name;
	
	// Elements that had no attribute value that could be faceted (ie. 'Other' or 'Unknown' checkbox in UI)
	private Integer noAttributeValueCount;
	
	private FacetFiltering filtering;

	public SearchFacetedAttributeResult(FacetFiltering filtering) {
		
		this.filtering = filtering;
	}

	public SearchFacetedAttributeResult(String id, String name, FacetFiltering filtering, Integer noAttributeValueCount) {
		
		this(filtering);
		
		if (id == null) {
			throw new IllegalArgumentException("id = null");
		}
		
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}
		
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
	
	public FacetFiltering getFiltering() {
		return filtering;
	}

	public void setFiltering(FacetFiltering filtering) {
		this.filtering = filtering;
	}

	public Integer getNoAttributeValueCount() {
		return noAttributeValueCount;
	}

	public void setNoAttributeValueCount(Integer noAttributeValueCount) {
		this.noAttributeValueCount = noAttributeValueCount;
	}
}
