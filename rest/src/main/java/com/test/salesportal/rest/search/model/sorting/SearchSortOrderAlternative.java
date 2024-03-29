package com.test.salesportal.rest.search.model.sorting;

public class SearchSortOrderAlternative {

	private String name;
	private String displayName;

	public SearchSortOrderAlternative() {
		
	}
	
	public SearchSortOrderAlternative(String name, String displayName) {
		
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		if (displayName == null) {
			throw new IllegalArgumentException("displayName == null");
		}

		this.name = name;
		this.displayName = displayName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		SearchSortOrderAlternative other = (SearchSortOrderAlternative) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
}
