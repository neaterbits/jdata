package com.test.cv.model.attributes;

import java.math.BigDecimal;
import java.util.Date;

public enum AttributeType {
	STRING,
	INTEGER,
	LONG,
	DECIMAL,
	ENUM,
	BOOLEAN,
	DATE;
	
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

