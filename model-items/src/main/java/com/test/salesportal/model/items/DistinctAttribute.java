package com.test.salesportal.model.items;

public class DistinctAttribute extends PropertyAttribute {

	private final String attributeName;
	private final Class<? extends Item> attributeDeclaringClass;

	public DistinctAttribute(ItemAttribute attribute) {
		super(attribute);

		this.attributeName = attribute.getName();
		this.attributeDeclaringClass = attribute.getDeclaringClass();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeDeclaringClass == null) ? 0 : attributeDeclaringClass.hashCode());
		result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
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
		DistinctAttribute other = (DistinctAttribute) obj;
		if (attributeDeclaringClass == null) {
			if (other.attributeDeclaringClass != null)
				return false;
		} else if (!attributeDeclaringClass.equals(other.attributeDeclaringClass))
			return false;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		return true;
	}
}
