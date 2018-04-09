package com.test.cv.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.test.cv.common.StringUtil;
import com.test.cv.model.annotations.SortableType;
import com.test.cv.model.attributes.ClassAttributes;

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

	public String encodeToString() {
		return getDeclaringClass().getSimpleName() + ':' + attributeName;
	}
	
	public static SortAttribute decode(Collection<Class<? extends Item>> allTypes, String s) {
		
		final String [] parts = StringUtil.split(s, ':');
		
		if (parts.length != 2) {
			throw new IllegalArgumentException("parts.length != 2: " + Arrays.toString(parts));
		}
		
		final String className = parts[0];
		
		final List<Class<? extends Item>> classes = allTypes.stream()
				.filter(cl -> cl.getSimpleName().equals(className))
				.collect(Collectors.toList());
		
		if (classes.isEmpty()) {
			throw new IllegalArgumentException("No class name for encoded class name " + className);
		}
		else if (classes.size() > 1) {
			throw new IllegalArgumentException("More than one class for class name " + className);
		}

		final String attributeName = parts[1];

		// Find the matching item attribute
		final ClassAttributes classAttributes = ClassAttributes.getFromClass(classes.get(0));
		
		final List<ItemAttribute> attributes = classAttributes.asSet().stream()
				.filter(a -> a.getName().equals(attributeName))
				.collect(Collectors.toList());
		
		if (attributes.size() == 0) {
			throw new IllegalArgumentException("No attribute found from " + s);
		}
		else if (attributes.size() > 1) {
			throw new IllegalArgumentException("More than one attribute found from " + s);
		}

		return attributes.get(0).makeSortAttribute();
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
