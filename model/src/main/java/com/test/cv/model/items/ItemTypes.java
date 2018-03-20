package com.test.cv.model.items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.annotations.FacetEntity;
import com.test.cv.model.attributes.ClassAttributes;
import com.test.cv.model.housing.RentalApartment;
import com.test.cv.model.items.sports.Ski;
import com.test.cv.model.items.sports.Snowboard;
import com.test.cv.model.items.vehicular.Boat;
import com.test.cv.model.items.vehicular.Car;

public class ItemTypes {
	
	
	private static final List<Class<? extends Item>> types = Arrays.asList(
			Snowboard.class,
			Ski.class,
			Boat.class,
			Car.class,
			RentalApartment.class,
			Item.class);
	
	private static final List<String> typeNames;
	
	private static final Map<String, TypeInfo> typesByName;
	
	static {
		typeNames = types.stream().map(t -> getTypeName(t)).collect(Collectors.toList());
	}
	
	public static Class<?> [] getTypeClasses() {
		return types.toArray(new Class<?>[types.size()]);
	}

	public static String [] getTypeNames() {
		return typeNames.toArray(new String[typeNames.size()]);
	}

	public static String getTypeName(Class<? extends Item> type) {
		return type.getSimpleName();
	}

	public static String getTypeName(Item item) {
		return getTypeName(getType(item));
	}

	// TODO get from annotation
	public static String getTypeDisplayName(Class<? extends Item> type) {
		return getTypeInfo(type).getFacetDisplayName();
	}
	
	public static Class<? extends Item> getType(Item item) {
		return item.getClass();
	}
	
	public static TypeInfo getTypeInfo(Class<? extends Item> type) {
		return getTypeByName(getTypeName(type));
	}
	
	public static TypeInfo getTypeInfo(Item item) {
		return getTypeInfo(getType(item));
	}
	
	public static Set<ItemAttribute> getFacetAttributes(String ... types) {
		final Set<ItemAttribute> facetAttributes = new HashSet<>();

		for (String typeName : types) {
			final TypeInfo typeInfo = getTypeByName(typeName);
			
			typeInfo.getAttributes().forEach(itemAttribute -> {
				if (itemAttribute.isFaceted()) {
					facetAttributes.add(itemAttribute);
				}
			});
		}
		
		return facetAttributes;
	}
	
	
	static {
		typesByName = new HashMap<>();
		
		for (Class<? extends Item> type : types) {
			final String typeName = getTypeName(type);
			
			if (typesByName.containsKey(typeName)) {
				throw new IllegalStateException("Already contains type with name " + typeName);
			}
			
			final ClassAttributes attributes = ClassAttributes.getFromClass(type);
			
			final FacetEntity facetEntity = type.getAnnotation(FacetEntity.class);
			
			final String facetTypeDisplayName = facetEntity != null ? facetEntity.value() : type.getSimpleName();
			
			typesByName.put(typeName, new TypeInfo(type, facetTypeDisplayName, attributes));
		}
	}
	
	public static TypeInfo getTypeByName(String typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException("typeName == null");
		}

		return typesByName.get(typeName);
	}

}
