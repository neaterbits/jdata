package com.test.salesportal.search.criteria;

import com.test.salesportal.model.items.ItemAttribute;

public abstract class Criterium {
	private final ItemAttribute attribute;

	public Criterium(ItemAttribute attribute) {

		if (attribute == null) {
			throw new IllegalArgumentException("property == null");
		}
		
		this.attribute = attribute;
	}

	public final ItemAttribute getAttribute() {
		return attribute;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
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
		Criterium other = (Criterium) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		return true;
	}
}
