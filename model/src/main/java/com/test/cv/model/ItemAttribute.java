package com.test.cv.model;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;

import com.test.cv.model.annotations.DecimalRange;
import com.test.cv.model.annotations.IntegerRange;
import com.test.cv.model.attributes.AttributeType;
import com.test.cv.model.attributes.facets.FacetedAttributeDecimalRange;
import com.test.cv.model.attributes.facets.FacetedAttributeIntegerRange;

// A searchable attribute for an item and accessor methods
public final class ItemAttribute {

	private final Class<? extends Item> itemType;
	private final PropertyDescriptor property;
	
	// Does this attrbute gas facets?
	private final boolean isFaceted;
	
	// Ay integer ranges if this is an integer attribute
	private final FacetedAttributeIntegerRange [] integerRanges;
	private final FacetedAttributeDecimalRange [] decimalRanges;

	public ItemAttribute(Class<? extends Item> itemType, PropertyDescriptor property,
				boolean isFaceted,
				IntegerRange [] integerRanges, DecimalRange [] decimalRanges) {
	
		if (itemType == null) {
			throw new IllegalArgumentException("itemType == null");
		}
		
		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}

		this.itemType = itemType;
		this.property = property;
		
		if (integerRanges != null && integerRanges.length > 0 && decimalRanges != null && decimalRanges.length > 0) {
			throw new IllegalArgumentException("Cannot have both integer and decimal ranges for property " + property.getName());
		}

		this.isFaceted = isFaceted;
		this.integerRanges = integerRanges == null || integerRanges.length == 0 ? null : convertIntegerRanges(integerRanges);
		this.decimalRanges = decimalRanges == null || decimalRanges.length == 0 ? null : convertDecimalRanges(decimalRanges);
	}
	
	private static FacetedAttributeIntegerRange[] convertIntegerRanges(IntegerRange [] ranges) {
		final FacetedAttributeIntegerRange [] result = new FacetedAttributeIntegerRange[ranges.length];
		
		for (int i = 0; i < ranges.length; ++ i) {
			result[i] = new FacetedAttributeIntegerRange(
					ranges[i].lower() == Integer.MIN_VALUE ? null : ranges[i].lower(),
					ranges[i].upper() == Integer.MAX_VALUE ? null : ranges[i].upper());
		}
		
		return result;
	}

	private static FacetedAttributeDecimalRange[] convertDecimalRanges(DecimalRange [] ranges) {
		final FacetedAttributeDecimalRange [] result = new FacetedAttributeDecimalRange[ranges.length];
		
		for (int i = 0; i < ranges.length; ++ i) {
			result[i] = new FacetedAttributeDecimalRange(
					ranges[i].lower() == Double.MIN_VALUE ? null : BigDecimal.valueOf(ranges[i].lower()),
					ranges[i].upper() == Double.MAX_VALUE ? null : BigDecimal.valueOf(ranges[i].upper()));
		}
		
		return result;
	}

	public Class<? extends Item> getItemType() {
		return itemType;
	}
	
	public String getName() {
		return property.getName();
	}

	// TODO use annotations
	public String getDisplayName() {
		return property.getName();
	}
	
	public AttributeType getAttributeType() {
		final Class<?> propertyType = property.getPropertyType();
		
		final AttributeType attributeType;
		
		if (propertyType.equals(String.class)) {
			attributeType = AttributeType.STRING;
		}
		else if (propertyType.equals(Integer.class) || propertyType.equals(int.class)) {
			attributeType = AttributeType.INTEGER;
		}
		else if (propertyType.equals(BigDecimal.class)) {
			attributeType = AttributeType.DECIMAL;
		}
		else {
			throw new IllegalStateException("Unknown property type " + propertyType + " of attribute " + getName() + " of " + itemType.getSimpleName());
		}

		return attributeType;
	}

	public boolean isFaceted() {
		return isFaceted;
	}

	public FacetedAttributeIntegerRange[] getIntegerRanges() {
		return integerRanges;
	}

	public FacetedAttributeDecimalRange[] getDecimalRanges() {
		return decimalRanges;
	}
}
