package com.test.salesportal.model.attributes;

import java.math.BigDecimal;
import java.util.Date;

import com.test.salesportal.model.annotations.SortableType;

public enum AttributeType {
	STRING(SortableType.ALPHABETICAL),
	INTEGER(SortableType.NUMERICAL),
	LONG(SortableType.NUMERICAL),
	DECIMAL(SortableType.NUMERICAL),
	ENUM(SortableType.ALPHABETICAL),
	BOOLEAN(SortableType.BOOLEAN),
	DATE(SortableType.TIME);
	
	AttributeType(SortableType sortableType) {
		this.sortableType = sortableType;
	}
	
	private final SortableType sortableType;
	
	public SortableType getSortableType() {
		return sortableType;
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
		else {
			attributeType = null;
		}

		return attributeType;
	}
}

