package com.test.salesportal.model.attributes;

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

import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.ItemAttributeValue;
import com.test.salesportal.model.annotations.DecimalRange;
import com.test.salesportal.model.annotations.Facet;
import com.test.salesportal.model.annotations.FacetAttribute;
import com.test.salesportal.model.annotations.FacetAttributes;
import com.test.salesportal.model.annotations.FacetEntity;
import com.test.salesportal.model.annotations.Freetext;
import com.test.salesportal.model.annotations.IndexItemAttribute;
import com.test.salesportal.model.annotations.IndexItemAttributeTransient;
import com.test.salesportal.model.annotations.IntegerRange;
import com.test.salesportal.model.annotations.Sortable;
import com.test.salesportal.model.items.ItemTypes;
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
	
	public Set<ItemAttribute> asSet() {
		return attributeSet;
	}
	
	public static List<ItemAttributeValue<?>> getValues(Item item) {
		final TypeInfo typeInfo = ItemTypes.getTypeInfo(item);
		
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
					
			final boolean isFacet;
			final String facetDisplayName;
			final String facetSuperAttribute;
			final IntegerRange [] integerRanges;
			final DecimalRange [] decimalRanges;
			final String trueString;
			final String falseString;

			final Facet fieldFacet = findAnnotation(Facet.class, type, propertyDescriptor);

			if (fieldFacet != null) {
				isFacet = true;
				facetDisplayName = fieldFacet.value();
				facetSuperAttribute = fieldFacet.superAttribute();
				integerRanges = fieldFacet.integerRanges();
				decimalRanges = fieldFacet.decimalRanges();
				trueString = fieldFacet.trueString();
				falseString = fieldFacet.falseString();
			}
			else {
				// Check if specified in subclass
				final FacetAttribute facetAttribute = facetAttributeMap.get(propertyDescriptor);
				
				if (facetAttribute != null) {
					isFacet = true;
					facetDisplayName = facetAttribute.displayName();
					facetSuperAttribute = facetAttribute.superAttribute();
					integerRanges = facetAttribute.integerRanges();
					decimalRanges = facetAttribute.decimalRanges();
					trueString = facetAttribute.trueString();
					falseString = facetAttribute.falseString();
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

			final boolean storeFieldInIndex;
			
			// Depends on attribute annotation
			final IndexItemAttribute indexItemAttribute = findAnnotation(IndexItemAttribute.class, type, propertyDescriptor);
	
			if (isFacet) {
				// Faceting requires store
				// TODO perhaps not for elasticsearh
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

			final ItemAttribute attribute = new ItemAttribute(
					type,
					propertyDescriptor,
					fieldNameOverride,
					storeFieldInIndex,
					isFreetext,
					isSortable,
					sortableTitle,
					sortablePriority,
					isFacet,
					facetDisplayName,
					facetSuperAttribute != null ? (facetSuperAttribute.trim().isEmpty() ? null : facetSuperAttribute.trim()) : null,
					integerRanges,
					decimalRanges,
					trueString,
					falseString);

			attributes.add(attribute);
		}

		return new ClassAttributes(type, attributes);
	}
	
	private static <T extends Annotation> T findAnnotation(Class<T> annotationType, Class<?> cl, PropertyDescriptor propertyDescriptor) {
		T annotation = propertyDescriptor.getReadMethod().getAnnotation(annotationType);
		
		if (annotation == null) {
			// Try get from field
			final String fieldName = propertyDescriptor.getName();

			Field found = null;
			
			for (Class<?> t = cl; t != null; t = t.getSuperclass()) {
				for (Field field : t.getDeclaredFields()) {
					if (field.getName().equals(fieldName)) {
						
						// Verify that not multiple fields of the same name
						if (found != null) {
							throw new IllegalStateException("Multiple fields named " + fieldName + " for " + cl);
						}
						
						found = field;
					}
				}
			}

			if (found != null) {
				annotation = found.getAnnotation(annotationType);
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
}