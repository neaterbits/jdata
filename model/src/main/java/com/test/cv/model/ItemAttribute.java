package com.test.cv.model;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
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
	
	private final String fieldNameOverride;

	private final boolean storeValueInSearchIndex;
	
	// Does this attribute have facets?
	private final boolean isFaceted;
	
	// Ay integer ranges if this is an integer attribute
	private final FacetedAttributeIntegerRange [] integerRanges;
	private final FacetedAttributeDecimalRange [] decimalRanges;

	public ItemAttribute(Class<? extends Item> itemType, PropertyDescriptor property,
				String fieldNameOverride,
				boolean storeValueInSearchIndex,
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
		
		this.fieldNameOverride = fieldNameOverride;
		
		if (isFaceted && !storeValueInSearchIndex) {
			// TODO is this the case for elasticsearch?
			throw new IllegalArgumentException("Ought always store faceted attribute values in index");
		}
		
		this.storeValueInSearchIndex = storeValueInSearchIndex;
		
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
		return fieldNameOverride != null ? fieldNameOverride : property.getName();
	}
	
	public ItemAttributeValue<?> getValue(Item item) {
		final Object value;
		try {
			value = property.getReadMethod().invoke(item);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new IllegalStateException("Failed to invoke getter for " + getName() + " of " + item.getClass().getName(), ex);
		}

		final ItemAttributeValue<?> itemAttributeValue;

		if (value != null) {
			switch (getAttributeType()) {
			case STRING:
				itemAttributeValue = new StringAttributeValue(this, (String)value);
				break;
				
			case INTEGER:
				itemAttributeValue = new IntegerAttributeValue(this, (Integer)value);
				break;

			case LONG:
				itemAttributeValue = new LongAttributeValue(this, (Long)value);
				break;

			case DECIMAL:
				itemAttributeValue = new DecimalAttributeValue(this, (BigDecimal)value);
				break;
				
			case ENUM:
				itemAttributeValue = new EnumAttributeValue(this, (Enum<?>)value);
				break;

			default:
				throw new UnsupportedOperationException("Unknown attribute type " + getAttributeType());
			}
 		}
		else {
			itemAttributeValue = null;
		}

		return itemAttributeValue;
	}

	// TODO use annotations
	public String getDisplayName() {
		return property.getName();
	}
	
	public AttributeType getAttributeType() {
		final Class<?> propertyType = property.getPropertyType();

		final AttributeType attributeType = AttributeType.fromClass(propertyType);

		if (attributeType == null) {
			throw new IllegalStateException("Unknown property type " + propertyType + " of attribute " + getName() + " of " + itemType.getSimpleName());
		}

		return attributeType;
	}

	
	public boolean shouldStoreValueInSearchIndex() {
		return storeValueInSearchIndex;
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
