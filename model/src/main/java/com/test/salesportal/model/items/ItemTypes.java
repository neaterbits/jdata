package com.test.salesportal.model.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.test.salesportal.model.DistinctAttribute;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.annotations.FacetEntity;
import com.test.salesportal.model.attributes.ClassAttributes;
import com.test.salesportal.model.housing.RentalApartment;
import com.test.salesportal.model.items.sports.DownhillSki;
import com.test.salesportal.model.items.sports.Ski;
import com.test.salesportal.model.items.sports.Snowboard;
import com.test.salesportal.model.items.vehicular.Boat;
import com.test.salesportal.model.items.vehicular.Car;

public class ItemTypes {
	
	private static final List<Class<? extends Item>> baseTypes = Collections.unmodifiableList(Arrays.asList(Item.class, BaseItem.class));
	
	private static final List<Class<? extends Item>> types = Collections.unmodifiableList(Arrays.asList(
			Snowboard.class,
			Ski.class,
			DownhillSki.class,
			Boat.class,
			Car.class,
			RentalApartment.class));
	
	private static final List<Class<? extends Item>> jaxbTypes;
	
	private static final List<String> typeNames;
	
	private static final Map<String, TypeInfo> typesByName;
	
	private static final Set<Class<? extends Item>> typesSet;
	
	static {
		typeNames = types.stream().map(t -> getTypeName(t)).collect(Collectors.toList());
		typesSet = Collections.unmodifiableSet(new HashSet<>(types));
		
		jaxbTypes = new ArrayList<>(baseTypes.size() + types.size());
		//jaxbTypes.addAll(baseTypes);
		jaxbTypes.addAll(types);
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Item> [] getJAXBTypeClasses() {
		return types.toArray(new Class[types.size()]);
	}

	public static List<Class<? extends Item>> getBaseTypesList() {
		return baseTypes;
	}

	public static Set<Class<? extends Item>> getBaseTypes(Collection<Class<? extends Item>> typesList) {
		
		final Set<Class<? extends Item>> baseTypes = new HashSet<>(typesList.size());
		
		for (Class<? extends Item> cl : typesList) {
			for (Class<?> superClass = cl.getSuperclass();
					Item.class.isAssignableFrom(superClass);
					superClass = superClass.getSuperclass()) {
				
				@SuppressWarnings("unchecked")
				final Class<? extends Item> baseType = (Class<? extends Item>)superClass;
				
				baseTypes.add(baseType);
			}
		}
		
		return baseTypes;
	}

	public static List<Class<? extends Item>> getAllTypesList() {
		return types;
	}

	public static List<TypeInfo> getAllTypeInfosList() {
		return types.stream()
				.map(type -> getTypeInfo(type))
				.collect(Collectors.toList());
	}

	public static Set<Class<? extends Item>> getAllTypesSet() {
		return typesSet;
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
	
	public static Set<ItemAttribute> getFacetAttributes(Collection<Class<? extends Item>> types) {
		final Set<ItemAttribute> facetAttributes = new HashSet<>();

		getAttributes(types, facetAttributes, a -> a.isFaceted(), a -> a);
		
		return facetAttributes;
	}

	public static Set<DistinctAttribute> getFreetextAttributes(Collection<Class<? extends Item>> types) {
		final Set<DistinctAttribute> attributes = new HashSet<>();

		getAttributes(types, attributes, a -> a.isFreetext(), a -> new DistinctAttribute(a));
		
		return attributes;
	}

	private static <T> void getAttributes(Collection<Class<? extends Item>> types, Collection<T> collection, Predicate<ItemAttribute> filter, Function<ItemAttribute, T> convert) {

		for (Class<? extends Item> type : types) {
			final TypeInfo typeInfo = getTypeInfo(type);
			
			typeInfo.getAttributes().forEach(itemAttribute -> {
				if (filter.test(itemAttribute)) {
					
					collection.add(convert.apply(itemAttribute));
				}
			});
		}
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
