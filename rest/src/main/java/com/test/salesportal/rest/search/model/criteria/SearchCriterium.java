package com.test.salesportal.rest.search.model.criteria;

public class SearchCriterium {
	private String type; // type of object this belongs to
	private String attribute; // attribute for this one
	
	// either a values ranges
	private SearchCriteriumValue [] values;
	private Boolean otherSelected;
	
	private SearchRange [] ranges;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getAttribute() {
		return attribute;
	}
	
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public SearchCriteriumValue[] getValues() {
		return values;
	}

	void setValues(SearchCriteriumValue[] values) {
		this.values = values;
	}

	public Boolean getOtherSelected() {
		return otherSelected;
	}

	public void setOtherSelected(Boolean otherSelected) {
		this.otherSelected = otherSelected;
	}

	public SearchRange[] getRanges() {
		return ranges;
	}

	void setRanges(SearchRange[] ranges) {
		this.ranges = ranges;
	}
}
