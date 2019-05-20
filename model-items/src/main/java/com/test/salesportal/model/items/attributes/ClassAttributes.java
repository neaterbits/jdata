package com.test.salesportal.model.items.attributes;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.test.salesportal.model.items.AttributeEnum;
import com.test.salesportal.model.items.FacetFiltering;
import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.ItemAttributeValue;
import com.test.salesportal.model.items.PropertyAttribute;
import com.test.salesportal.model.items.annotations.DecimalRange;
import com.test.salesportal.model.items.annotations.DisplayAttribute;
import com.test.salesportal.model.items.annotations.Facet;
import com.test.salesportal.model.items.annotations.FacetAttribute;
import com.test.salesportal.model.items.annotations.FacetAttributes;
import com.test.salesportal.model.items.annotations.FacetEntity;
import com.test.salesportal.model.items.annotations.Freetext;
import com.test.salesportal.model.items.annotations.IndexItemAttribute;
import com.test.salesportal.model.items.annotations.IndexItemAttributeTransient;
import com.test.salesportal.model.items.annotations.IntegerRange;
import com.test.salesportal.model.items.annotations.NumericAttributeFiltering;
import com.test.salesportal.model.items.annotations.ServiceAttribute;
import com.test.salesportal.model.items.annotations.Sortable;
import com.test.salesportal.model.items.annotations.UpdateFacetDisplayName;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;

public class ClassAttributes {

	private final Class<? extends Item> type;
	private final List<ItemAttribute> attributes;
	private final Set<ItemAttribute> attributeSet;

	private ClassAttributes(Class<? extends Item> type, List<ItemAttribute> attributes) {
		this.type = type;
		this.attributes = attributes;
		this.attributeSet = Collections.unmodifiableSet(new HashSet<>(attributes));
	}
	
	public List<ItemAttribute> sortInFacetOrder(Collection<ItemAttribute> attributes, boolean checkAllPropertiesPresentInList) {
		final List<ItemAttribute> attributesInFacetOrder;
		final FacetEntity facetEntity = type.getAnnotation(FacetEntity.class);

		if (facetEntity != null && facetEntity.propertyOrder() != null) {
			attributesInFacetOrder = new ArrayList<>(attributes.size());
			
			// First find all attributes in property order
			for (String property : facetEntity.propertyOrder()) {
				ItemAttribute found = null;
				
				for (ItemAttribute attribute : attributes) {
					if (attribute.getName().equals(property)) {
						found = attribute;
						break;
					}
				}

				if (found == null) {
					if (checkAllPropertiesPresentInList) {
						throw new IllegalStateException("No attribute with property name " + property + " from facet entity order list");
					}
					
					// System.err.println("Could not find attr " + property);
				}
				else {
					if (attributesInFacetOrder.contains(found)) {
						throw new IllegalStateException("Already added property from property order list: " + property);
					}
					
					attributesInFacetOrder.add(found);
				}
			}
			
			// Now add all that are not specified in order
			for (ItemAttribute attribute : attributes) {
				if (!attributesInFacetOrder.contains(attribute)) {
					attributesInFacetOrder.add(attribute);
				}
			}
		}
		else {
			attributesInFacetOrder = new ArrayList<>(attributes);
		}
		
		return attributesInFacetOrder;
	}
	
	public int getAutoExpandPropertiesCount() {
		
		final FacetEntity facetEntity = type.getAnnotation(FacetEntity.class);
		
		return facetEntity != null
				? facetEntity.expandProperties()
				: 0;
	}
	
	public Set<ItemAttribute> asSet() {
		return attributeSet;
	}
	
	public static List<ItemAttributeValue<?>> getValues(ItemTypes itemTypes, Item item) {
		final TypeInfo typeInfo = itemTypes.getTypeInfo(item);
		
		final ClassAttributes classAttributes = typeInfo.getAttributes();
		
		final List<ItemAttributeValue<?>> result = new ArrayList<>();
		
		for (ItemAttribute attribute : classAttributes.attributes) {
			final ItemAttributeValue<?> value = attribute.getValue(item);
			
			if (value != null) {
				result.add(value);
			}
		}
		
		return result;
	}
	
	public static ClassAttributes getFromClass(Class<? extends Item> type) {

		final BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(type);
		} catch (IntrospectionException ex) {
			throw new IllegalStateException("Failed to get bean info", ex);
		}

		final List<ItemAttribute> attributes = new ArrayList<>(beanInfo.getPropertyDescriptors().length);

		// Must collect facet information from class and from the attribute
		// For shared attributes in base classes, might override in FacetAttribute annotation
		final Annotation [] annotations = type.getAnnotations();

		final Map<PropertyDescriptor, FacetAttribute> facetAttributeMap
					= new HashMap<>();

		for (Annotation classAnnotation : annotations) {

			if (classAnnotation.annotationType().equals(FacetAttribute.class)) {
				final FacetAttribute facetAttribute = (FacetAttribute)classAnnotation;

				addFacetAttribute(facetAttributeMap, facetAttribute, beanInfo);
			}
			else if (classAnnotation instanceof FacetAttributes) {
				final FacetAttributes facetAttributes = (FacetAttributes)classAnnotation;

				for (FacetAttribute facetAttribute : facetAttributes.value()) {
					addFacetAttribute(facetAttributeMap, facetAttribute, beanInfo);
				}
			}
		}

		for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {

			if (propertyDescriptor.getReadMethod() == null || propertyDescriptor.getWriteMethod() == null) {
				continue;
			}

			if (findAnnotation(IndexItemAttributeTransient.class, type, propertyDescriptor) != null) {
				// Not for indexing
				continue;
			}

			final boolean isFreetext = findAnnotation(Freetext.class, type, propertyDescriptor) != null;

			final Sortable sortableAnnotation = findAnnotation(Sortable.class, type, propertyDescriptor);

			final boolean isSortable;
			final String sortableTitle;
			final int sortablePriority;

			if (sortableAnnotation != null) {
				isSortable = true;
				
				final String trimmed = sortableAnnotation.value().trim();
				sortableTitle = trimmed.isEmpty() ? null : trimmed;
				sortablePriority = sortableAnnotation.priority();
			}
			else {
				isSortable = false;
				sortableTitle = null;
				sortablePriority = -1;
			}
			
			final DisplayAttribute displayAttribute = findAnnotation(DisplayAttribute.class, type, propertyDescriptor);
		
			final boolean isFacet;
			String facetDisplayName;
			final String facetSuperAttribute;
			final IntegerRange [] integerRanges;
			final DecimalRange [] decimalRanges;
			final String trueString;
			final String falseString;

			final Facet fieldFacet = findAnnotation(Facet.class, type, propertyDescriptor);

			if (fieldFacet != null) {
				isFacet = true;
				facetDisplayName = getStringValue(fieldFacet, f -> f.displayName(), displayAttribute, d -> d.value());
				facetSuperAttribute = fieldFacet.superAttribute();
				integerRanges = fieldFacet.integerRanges();
				decimalRanges = fieldFacet.decimalRanges();
				trueString = getStringValue(fieldFacet, f -> f.trueString(), displayAttribute, d -> d.trueString());
				falseString = getStringValue(fieldFacet, f -> f.falseString(), displayAttribute, d -> d.falseString());
			}
			else {
				// Check if specified in subclass
				final FacetAttribute facetAttribute = facetAttributeMap.get(propertyDescriptor);
				
				if (facetAttribute != null) {
					isFacet = true;
					facetDisplayName = getStringValue(facetAttribute, f -> f.displayName(), displayAttribute, d -> d.value());
					facetSuperAttribute = facetAttribute.superAttribute();
					integerRanges = facetAttribute.integerRanges();
					decimalRanges = facetAttribute.decimalRanges();
					trueString = getStringValue(facetAttribute, f -> f.trueString(), displayAttribute, d -> d.trueString());
					falseString = getStringValue(facetAttribute, f -> f.falseString(), displayAttribute, d -> d.falseString());
				}
				else {
					isFacet = false;
					facetDisplayName = null;
					facetSuperAttribute = null;
					integerRanges = null;
					decimalRanges = null;
					trueString = null;
					falseString = null;
				}
			}
			
			final FacetFiltering facetFiltering;
			if (isFacet) {
				
				final AttributeType attributeType = AttributeType.fromClass(propertyDescriptor.getPropertyType());
				
				if (attributeType.isNumeric()) {
					final NumericAttributeFiltering numericFacetFiltering = findAnnotation(NumericAttributeFiltering.class, type, propertyDescriptor);
				
					if (numericFacetFiltering != null) {
						facetFiltering = numericFacetFiltering.value();
					}
					else if (hasRanges(integerRanges, decimalRanges)) {
						facetFiltering = FacetFiltering.RANGES;
					}
					else {
						facetFiltering = FacetFiltering.VALUE;
					}
				}
				else {
					facetFiltering = FacetFiltering.VALUE;
				}
			}
			else {
				facetFiltering = null;
			}

			final boolean storeFieldInIndex;
			
			// Depends on attribute annotation
			final IndexItemAttribute indexItemAttribute = findAnnotation(IndexItemAttribute.class, type, propertyDescriptor);
	
			if (isFacet || isSortable) {
				// Faceting requires store
				// TODO perhaps not for elasticsearch
				storeFieldInIndex = true;
			}
			else {
				if (indexItemAttribute != null) {
					storeFieldInIndex = indexItemAttribute.storeValue();
				}
				else {
					storeFieldInIndex = false; // Default to not store
				}
			}
			
			final String fieldNameOverride = indexItemAttribute != null && ! indexItemAttribute.name().isEmpty()
					? indexItemAttribute.name()
					: null;


			final String attributeName = PropertyAttribute.getName(fieldNameOverride, propertyDescriptor);

			if (isFacet) {
				final UpdateFacetDisplayName updateFacetDisplayName = type.getAnnotation(UpdateFacetDisplayName.class);
				
				if (updateFacetDisplayName != null && updateFacetDisplayName.attributeName().equals(attributeName)) {
					facetDisplayName = updateFacetDisplayName.updatedDisplayName();
				}
			}
			
			final ServiceAttribute serviceAttribute = findAnnotation(
					ServiceAttribute.class,
					type,
					propertyDescriptor);
					
			final String serviceAttributeName;
			
			if (serviceAttribute != null) {
				serviceAttributeName = getStringValue(
						serviceAttribute,
						sa -> sa.name(),
						attributeName,
						Function.identity());
			}
			else {
				serviceAttributeName = null;
			}
			
			final ItemAttribute attribute = new ItemAttribute(
					type,
					propertyDescriptor,
					fieldNameOverride,
					storeFieldInIndex,
					isFreetext,
					isSortable,
					sortableTitle,
					sortablePriority,
					serviceAttributeName,
					displayAttribute != null ? displayAttribute.value().trim() : null,
					isFacet,
					facetDisplayName,
					facetSuperAttribute != null ? (facetSuperAttribute.trim().isEmpty() ? null : facetSuperAttribute.trim()) : null,
					facetFiltering,
					integerRanges,
					decimalRanges,
					trueString,
					falseString);

			attributes.add(attribute);
		}

		return new ClassAttributes(type, attributes);
	}
	
	private static <T, U> String getStringValue(T value1, Function<T, String> value1String, U value2, Function<U, String> value2String) {
		
		final String value1Str = getStringValueOrNull(value1, value1String);
		
		return value1Str != null ? value1Str : getStringValueOrNull(value2, value2String);
	}

	
	private static <T> String getStringValueOrNull(T value1, Function<T, String> value1String) {
		
		String valueStr;
		
		if (value1 == null) {
			valueStr = null;
		}
		else {
			valueStr = value1String.apply(value1);
			
			final String trimmed = valueStr.trim();
			
			if (trimmed.isEmpty()) {
				valueStr = null;
			}
			else {
				valueStr = trimmed;
			}
		}
		
		return valueStr;
	}

	
	private static boolean hasRanges(IntegerRange [] integerRanges, DecimalRange [] decimalRanges) {
		return     (integerRanges != null && integerRanges.length > 0)
				|| (decimalRanges != null && decimalRanges.length > 0);
	}
	
	private static <T extends Annotation> T findAnnotation(Class<T> annotationType, Class<?> cl, PropertyDescriptor propertyDescriptor) {
		T annotation = propertyDescriptor.getReadMethod().getAnnotation(annotationType);
		
		if (annotation == null) {
			// Try get from field
			final String fieldName = propertyDescriptor.getName();

			Field foundField = null;
			
			for (Class<?> t = cl; t != null; t = t.getSuperclass()) {
				for (Field field : t.getDeclaredFields()) {
					if (field.getName().equals(fieldName)) {
						
						// Verify that not multiple fields of the same name
						if (foundField != null) {
							throw new IllegalStateException("Multiple fields named " + fieldName + " for " + cl);
						}
						
						foundField = field;
					}
				}
			}

			if (foundField != null) {
				annotation = foundField.getAnnotation(annotationType);
			}
		}
		
		return annotation;
	}

	private static void addFacetAttribute(
			Map<PropertyDescriptor, FacetAttribute> map,
			FacetAttribute facetAttribute,
			BeanInfo beanInfo) {
		
		// Find property descriptor by attribute name
		
		PropertyDescriptor found = null;
		
		for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
			
			if (propertyDescriptor.getName().equals(facetAttribute.name())) {
				found = propertyDescriptor;
				break;
			}
		}

		if (found == null) {
			throw new IllegalStateException("Could not find facet for attribute with name " + facetAttribute.name());
		}
		
		if (map.containsKey(found)) {
			throw new IllegalStateException("Already has facet for attribute with name " + facetAttribute.name());
		}
		
		map.put(found, facetAttribute);
	}
 	
	public ItemAttribute getByName(String name) {
		return attributes.stream()
				.filter(attribute -> attribute.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
	
	public void forEach(Consumer<ItemAttribute> consumer) {
		attributes.forEach(consumer);
	}

	public static Object getAttributeDisplayValue(ItemAttribute attribute, Object value) {
		final Object displayValue;
		
		if (value instanceof AttributeEnum) {
			displayValue = ((AttributeEnum)value).getDisplayName();
		}
		else if (value instanceof Boolean) {
			final boolean val = (Boolean)value;
			
			if (val && attribute.getTrueString() != null) {
				displayValue = attribute.getTrueString();
			}
			else if (!val && attribute.getFalseString() != null) {
				displayValue = attribute.getFalseString();
			}
			else {
				displayValue = value;
			}
		}
		else {
			displayValue = value;
		}

		return displayValue;
	}
}

