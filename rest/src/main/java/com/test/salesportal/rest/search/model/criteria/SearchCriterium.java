package com.test.salesportal.rest.search.model.criteria;

import java.util.Arrays;

public class SearchCriterium {
	private String type; // type of object this belongs to
	private String attribute; // attribute for this one

	// A set of values
	private SearchCriteriumValue [] values;
	private Boolean otherSelected;
	
	// or a set of ranges
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((otherSelected == null) ? 0 : otherSelected.hashCode());
		result = prime * result + Arrays.hashCode(ranges);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + Arrays.hashCode(values);
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
		SearchCriterium other = (SearchCriterium) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		if (otherSelected == null) {
			if (other.otherSelected != null)
				return false;
		} else if (!otherSelected.equals(other.otherSelected))
			return false;
		if (!Arrays.equals(ranges, other.ranges))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}
}
