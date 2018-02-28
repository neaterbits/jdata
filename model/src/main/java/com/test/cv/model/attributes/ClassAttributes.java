package com.test.cv.model.attributes;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.annotations.DecimalRange;
import com.test.cv.model.annotations.Facet;
import com.test.cv.model.annotations.FacetAttribute;
import com.test.cv.model.annotations.FacetAttributes;
import com.test.cv.model.annotations.IntegerRange;

public class ClassAttributes {

	private final Class<? extends Item> type;
	private final List<ItemAttribute> attributes;

	private ClassAttributes(Class<? extends Item> type, List<ItemAttribute> attributes) {
		this.type = type;
		this.attributes = attributes;
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
			
			Facet fieldFacet = null;
			
			fieldFacet = propertyDescriptor.getReadMethod().getAnnotation(Facet.class);
			
			if (fieldFacet == null) {
				// Try get from field
				final String fieldName = propertyDescriptor.getName();

				Field found = null;
				
				for (Class<?> t = type; t != null; t = t.getSuperclass()) {
					for (Field field : t.getDeclaredFields()) {
						if (field.getName().equals(fieldName)) {
							
							// Verify that not multiple fields of the same name
							if (found != null) {
								throw new IllegalStateException("Multiple fields named " + fieldName + " for " + type);
							}
							
							found = field;
						}
					}
				}

				if (found != null) {
					fieldFacet = found.getAnnotation(Facet.class);
				}
			}

			final boolean isFacet;
			final IntegerRange [] integerRanges;
			final DecimalRange [] decimalRanges;

			if (fieldFacet != null) {
				isFacet = true;
				integerRanges = fieldFacet.integerRanges();
				decimalRanges = fieldFacet.decimalRanges();
			}
			else {
				// Check if specified in subclass
				final FacetAttribute facetAttribute = facetAttributeMap.get(propertyDescriptor);
				
				if (facetAttribute != null) {
					isFacet = true;
					integerRanges = facetAttribute.integerRanges();
					decimalRanges = facetAttribute.decimalRanges();
				}
				else {
					isFacet = false;
					integerRanges = null;
					decimalRanges = null;
				}
			}

			final ItemAttribute attribute = new ItemAttribute(type, propertyDescriptor, isFacet, integerRanges, decimalRanges);

			attributes.add(attribute);
		}

		return new ClassAttributes(type, attributes);
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
}
