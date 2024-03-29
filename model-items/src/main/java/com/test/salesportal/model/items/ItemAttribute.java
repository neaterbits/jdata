package com.test.salesportal.model.items;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Date;

import com.test.salesportal.model.items.annotations.DecimalRange;
import com.test.salesportal.model.items.annotations.IntegerRange;
import com.test.salesportal.model.items.annotations.SortableType;
import com.test.salesportal.model.items.attributes.facets.FacetedAttributeDecimalRange;
import com.test.salesportal.model.items.attributes.facets.FacetedAttributeIntegerRange;

// A searchable attribute for an item and accessor methods
public final class ItemAttribute extends PropertyAttribute {
	
	private final Class<? extends Item> itemType;
	private final PropertyDescriptor property;
	
	private final boolean storeValueInSearchIndex;

	private final boolean isSortable;
	private final String sortableTitle;
	private final int sortablePriority;
	private final boolean isFreetext;
	
	private final String serviceAttributeName;
	private final String displayAttributeName;
	
	// Does this attribute have facets?
	private final boolean isFaceted;
	private final String facetDisplayName;
	private final FacetFiltering facetFiltering;
	
	// For sub-attributes
	private final String facetSuperAttribute;
	
	// Ay integer ranges if this is an integer attribute
	private final FacetedAttributeIntegerRange [] integerRanges;
	private final FacetedAttributeDecimalRange [] decimalRanges;
	
	// For boolean attributes
	private final String trueString;
	private final String falseString;

	public ItemAttribute(Class<? extends Item> itemType, PropertyDescriptor property,
				String fieldNameOverride,
				boolean storeValueInSearchIndex,
				boolean isFreetext,
				boolean isSortable,
				String sortableTitle,
				int sortablePriority,
				String serviceAttributeName,
				String displayAttributeName,
				boolean isFaceted,
				String facetDisplayName,
				String facetSuperAttribute,
				FacetFiltering facetFiltering,
				IntegerRange [] integerRanges, DecimalRange [] decimalRanges,
				String trueString, String falseString) {
		
		super(property, fieldNameOverride);
	
		if (itemType == null) {
			throw new IllegalArgumentException("itemType == null");
		}
		
		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}
		
		if (isFaceted && facetFiltering == null) {
			throw new IllegalArgumentException("facetFiltering == null");
		}
		
		this.itemType = itemType;
		this.property = property;
		
		this.isFreetext = isFreetext;
		this.isSortable = isSortable;
		this.sortableTitle = sortableTitle;
		this.sortablePriority = sortablePriority;

		if (isFaceted && !storeValueInSearchIndex) {
			// TODO is this the case for elasticsearch?
			throw new IllegalArgumentException("Ought always store faceted attribute values in index");
		}
		
		this.storeValueInSearchIndex = storeValueInSearchIndex;
		
		this.serviceAttributeName = serviceAttributeName;
		this.displayAttributeName = displayAttributeName;
		
		if (integerRanges != null && integerRanges.length > 0 && decimalRanges != null && decimalRanges.length > 0) {
			throw new IllegalArgumentException("Cannot have both integer and decimal ranges for property " + property.getName());
		}

		this.isFaceted = isFaceted;
		this.facetDisplayName = facetDisplayName;
		this.facetFiltering = facetFiltering;
		this.facetSuperAttribute = facetSuperAttribute;
		this.integerRanges = integerRanges == null || integerRanges.length == 0 ? null : convertIntegerRanges(integerRanges);
		this.decimalRanges = decimalRanges == null || decimalRanges.length == 0 ? null : convertDecimalRanges(decimalRanges);

		this.trueString = trueString != null ? trueString : "true";
		this.falseString = falseString != null ? falseString : "false";
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
	
	public boolean isSingleValue() {
		return integerRanges == null && decimalRanges == null;
	}

	public boolean isRange() {
		return !isSingleValue();
	}

	public Object getObjectValue(Item item) {
		final Object value;
		try {
			value = property.getReadMethod().invoke(item);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new IllegalStateException("Failed to invoke getter for " + getName() + " of " + item.getClass().getName(), ex);
		}

		return value;
	}
	
	public ItemAttributeValue<?> getValue(Item item) {
		final Object value = getObjectValue(item);

		return getValueFromObject(value);
	}

	public ItemAttributeValue<?> getValueFromObject(Object value) {

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
				
			case BOOLEAN:
				itemAttributeValue = new BooleanAttributeValue(this, (Boolean)value);
				break;
				
			case DATE:
				itemAttributeValue = new DateAttributeValue(this, (Date)value);
				break;
				
			case TZDATE:
				itemAttributeValue = new TZDateAttributeValue(this, (OffsetDateTime)value);
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
	
	public boolean shouldStoreValueInSearchIndex() {
		return storeValueInSearchIndex;
	}

	public boolean isSortable() {
		return isSortable;
	}

	public String getSortableTitle() {

		final String result;

		if (this.sortableTitle != null) {
			result = this.sortableTitle;
		}
		else if (this.getFacetDisplayName() != null) {
			result = this.getFacetDisplayName();
		}
		else {
			result = this.getName();
		}
		
		return result;
	}

	public SortableType getSortableType() {
		return getAttributeType().getSortableType();
	}

	public int getSortablePriority() {
		return sortablePriority;
	}
	
	public SortAttribute makeSortAttribute() {
		
		if (!isSortable) {
			throw new IllegalArgumentException("Attr not sortable: " + getName());
		}

		return new SortAttribute(this);
	}

	public boolean isFreetext() {
		return isFreetext;
	}

	public boolean isServiceAttribute() {
		return serviceAttributeName != null;
	}

	public String getServiceAttributeName() {
		return serviceAttributeName;
	}

	public boolean isDisplayAttribute() {
		return displayAttributeName != null;
	}
	
	public String getDisplayAttributeName() {
		return displayAttributeName;
	}

	public boolean isFaceted() {
		return isFaceted;
	}

	public String getFacetDisplayName() {
		return facetDisplayName != null ? facetDisplayName : property.getName();
	}

	public String getFacetSuperAttribute() {
		return facetSuperAttribute;
	}
	
	public FacetFiltering getFacetFiltering() {
		return facetFiltering;
	}

	public FacetedAttributeIntegerRange[] getIntegerRanges() {
		return integerRanges;
	}

	public FacetedAttributeDecimalRange[] getDecimalRanges() {
		return decimalRanges;
	}
	
	public String getTrueString() {
		return trueString;
	}

	public String getFalseString() {
		return falseString;
	}

	public int getRangeCount() {
		
		if (!isRange()) {
			throw new UnsupportedOperationException("Not a range attribute");
		}
		
		return getIntegerRanges() != null
					? getIntegerRanges().length
					: getDecimalRanges().length;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itemType == null) ? 0 : itemType.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
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
		ItemAttribute other = (ItemAttribute) obj;
		if (itemType == null) {
			if (other.itemType != null)
				return false;
		} else if (!itemType.equals(other.itemType))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getName();
	}
}
