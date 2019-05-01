package com.test.salesportal.model;

import java.beans.PropertyDescriptor;

import com.test.salesportal.model.attributes.AttributeType;

public abstract class PropertyAttribute {

	private final PropertyDescriptor property;
	private final String fieldNameOverride;
	
	PropertyAttribute(PropertyDescriptor property, String fieldNameOverride) {
		
		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}

		this.property = property;
		this.fieldNameOverride = fieldNameOverride;
	}
	
	PropertyAttribute(PropertyAttribute other) {
		this.property = other.property;
		this.fieldNameOverride = other.fieldNameOverride;
	}

	public final String getName() {
		return fieldNameOverride != null ? fieldNameOverride : property.getName();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final Class<? extends Item> getDeclaringClass() {
		return (Class)property.getReadMethod().getDeclaringClass();
	}

	public final AttributeType getAttributeType() {
		final Class<?> propertyType = property.getPropertyType();

		final AttributeType attributeType = AttributeType.fromClass(propertyType);

		if (attributeType == null) {
			throw new IllegalStateException("Unknown property type " + propertyType + " of attribute " + getName());
		}

		return attributeType;
	}

	public final Class<?> getAttributeValueClass() {
		return property.getPropertyType();
	}
}
