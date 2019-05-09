package com.test.salesportal.model;

public class SortAttributeAndOrder {

	private final SortAttribute attribute;
	private final SortOrder sortOrder;
	
	public SortAttributeAndOrder(SortAttribute attribute, SortOrder sortOrder) {
		
		if (attribute == null) {
			throw new IllegalArgumentException("attribute == null");
		}
		
		if (sortOrder == null) {
			throw new IllegalArgumentException("sortOrder == null");
		}
		
		this.attribute = attribute;
		this.sortOrder = sortOrder;
	}

	public SortAttribute getAttribute() {
		return attribute;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((sortOrder == null) ? 0 : sortOrder.hashCode());
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
		SortAttributeAndOrder other = (SortAttributeAndOrder) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		if (sortOrder != other.sortOrder)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SortAttributeAndOrder [attribute=" + attribute + ", sortOrder=" + sortOrder + "]";
	}
}
