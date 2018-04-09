package com.test.cv.model;

import java.util.Comparator;

import com.test.cv.model.annotations.SortableType;

/**
 * SortAttributes are different from item attributes in that
 * a bas class attribute is common among subclasses, eg 'Title'
 * has same hashCode() and equals() for whether being in Car or Snowboard type
 * since resides in baseclass.
 * 
 */
public final class SortAttribute extends PropertyAttribute {

	public static Comparator<SortAttribute> SORTABLE_PRIORITY_COMPARATOR = new Comparator<SortAttribute>() {
		
		@Override
		public int compare(SortAttribute attr1, SortAttribute attr2) {
			
			// - first since higher value means higher pri
			int result = - Integer.compare(attr1.sortablePriority, attr2.sortablePriority);
			
			if (result == 0) {
				// Same priority, order by name
				result = attr1.sortableTitle.compareTo(attr2.sortableTitle);
			}
			
			return result;
		}
	};

	private final String attributeName;
	private final String sortableTitle;
	private final SortableType sortableType;
	private final int sortablePriority;
	private final Class<? extends Item> attributeDeclaringClass;
	
	SortAttribute(ItemAttribute attribute) {
		super(attribute);
		
		this.attributeName = attribute.getName();
		this.sortableTitle = attribute.getSortableTitle();
		this.sortableType = attribute.getSortableType();
		this.sortablePriority = attribute.getSortablePriority();
		this.attributeDeclaringClass = attribute.getDeclaringClass();
	}

	public String getSortableTitle() {
		return sortableTitle;
	}
	
	public SortableType getSortableType() {
		return sortableType;
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
		SortAttribute other = (SortAttribute) obj;
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
