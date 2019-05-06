package com.test.salesportal.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.test.salesportal.common.StringUtil;
import com.test.salesportal.model.annotations.SortableType;
import com.test.salesportal.model.attributes.ClassAttributes;
import com.test.salesportal.model.items.ItemTypes;

/**
 * SortAttributes are different from item attributes in that
 * a base class attribute is common among subclasses, eg 'Title'
 * has same hashCode() and equals() for whether being in Car or Snowboard type
 * since resides in baseclass.
 * 
 */
public final class SortAttribute extends DistinctAttribute {

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

	private final String sortableTitle;
	private final SortableType sortableType;
	private final int sortablePriority;
	
	SortAttribute(ItemAttribute attribute) {
		super(attribute);
		
		this.sortableTitle = attribute.getSortableTitle();
		this.sortableType = attribute.getSortableType();
		this.sortablePriority = attribute.getSortablePriority();
	}

	public String encodeToString() {
		return getDeclaringClass().getSimpleName() + ':' + getName();
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

	public Object getObjectValue(Item item) {
		
		final ItemAttribute itemAttribute = ItemTypes.getTypeInfo(item).getAttributes().getByName(getName());
		
		return itemAttribute.getObjectValue(item);
	}
	
	@Override
	public String toString() {
		return sortableTitle;
	}
}
