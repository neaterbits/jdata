package com.test.salesportal.model.items.attributes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Date;

import com.test.salesportal.model.items.annotations.SortableType;

public enum AttributeType {
	STRING(SortableType.ALPHABETICAL),
	INTEGER(SortableType.NUMERICAL),
	LONG(SortableType.NUMERICAL),
	DECIMAL(SortableType.NUMERICAL),
	ENUM(SortableType.ALPHABETICAL),
	BOOLEAN(SortableType.BOOLEAN),
	DATE(SortableType.TIME),
	TZDATE(SortableType.TIME);
	
	AttributeType(SortableType sortableType) {
		this.sortableType = sortableType;
	}
	
	private final SortableType sortableType;
	
	public SortableType getSortableType() {
		return sortableType;
	}

	public boolean isNumeric() {
		return sortableType == SortableType.NUMERICAL;
	}
	
	public static AttributeType fromClass(Class<?> propertyType) {
		final AttributeType attributeType;
		
		if (propertyType.equals(String.class)) {
			attributeType = AttributeType.STRING;
		}
		else if (propertyType.equals(Integer.class) || propertyType.equals(int.class)) {
			attributeType = AttributeType.INTEGER;
		}
		else if (propertyType.equals(Long.class) || propertyType.equals(long.class)) {
			attributeType = AttributeType.LONG;
		}
		else if (Enum.class.isAssignableFrom(propertyType)) {
			attributeType = AttributeType.ENUM;
		}
		else if (propertyType.equals(BigDecimal.class)) {
			attributeType = AttributeType.DECIMAL;
		}
		else if (propertyType.equals(Boolean.class) || propertyType.equals(boolean.class)) {
			attributeType = AttributeType.BOOLEAN;
		}
		else if (propertyType.equals(Date.class)) {
			attributeType = AttributeType.DATE;
		}
		else if (propertyType.equals(OffsetDateTime.class)) {
			attributeType = AttributeType.TZDATE;
		}
		else {
			attributeType = null;
		}

		return attributeType;
	}
	
	public Comparator<Object> makeObjectValueComparator() {
		final Comparator<Object> comparator;
		
		switch (this) {
		
		case INTEGER:
			comparator = (obj1, obj2) -> Integer.compare((Integer)obj1, (Integer)obj2);
			break;
			
		case LONG:
			comparator = (obj1, obj2) -> Long.compare((Long)obj1, (Long)obj2);
			break;
			
		case DECIMAL:
			comparator = (obj1, obj2) -> ((BigDecimal)obj1).compareTo((BigDecimal)obj2);
			break;
			
		case DATE:
			comparator = (obj1, obj2) -> ((Date)obj1).compareTo((Date)obj2);
			break;
			
		default:
			throw new UnsupportedOperationException();
		
		}

		return comparator;
	}
}

