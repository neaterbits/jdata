package com.test.salesportal.rest.search.model.criteria;

public class SearchRange {

	private Object lower;
	private boolean includeLower;
	private Object upper;
	private boolean includeUpper;
	
	public Object getLower() {
		return lower;
	}
	
	public void setLower(Object lower) {
		this.lower = lower;
	}
	
	
	public boolean includeLower() {
		return includeLower;
	}

	public void setIncludeLower(boolean includeLower) {
		this.includeLower = includeLower;
	}

	public Object getUpper() {
		return upper;
	}

	public void setUpper(Object upper) {
		this.upper = upper;
	}

	public boolean includeUpper() {
		return includeUpper;
	}

	public void setIncludeUpper(boolean includeUpper) {
		this.includeUpper = includeUpper;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (includeLower ? 1231 : 1237);
		result = prime * result + (includeUpper ? 1231 : 1237);
		result = prime * result + ((lower == null) ? 0 : lower.hashCode());
		result = prime * result + ((upper == null) ? 0 : upper.hashCode());
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
		SearchRange other = (SearchRange) obj;
		if (includeLower != other.includeLower)
			return false;
		if (includeUpper != other.includeUpper)
			return false;
		if (lower == null) {
			if (other.lower != null)
				return false;
		} else if (!lower.equals(other.lower))
			return false;
		if (upper == null) {
			if (other.upper != null)
				return false;
		} else if (!upper.equals(other.upper))
			return false;
		return true;
	}
}
