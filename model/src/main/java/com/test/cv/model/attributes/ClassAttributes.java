package com.test.cv.model.attributes;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;

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

		for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
			
			final ItemAttribute attribute = new ItemAttribute(type, propertyDescriptor);
			
			attributes.add(attribute);
		}
		
		return new ClassAttributes(type, attributes);
	}
	
	public ItemAttribute getByName(String name) {
		return attributes.stream()
				.filter(attribute -> attribute.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
