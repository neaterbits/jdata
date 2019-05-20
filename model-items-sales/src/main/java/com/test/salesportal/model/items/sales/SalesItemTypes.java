package com.test.salesportal.model.items.sales;

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

import com.test.salesportal.model.items.housing.RentalApartment;
import com.test.salesportal.model.items.DistinctAttribute;
import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.annotations.FacetEntity;
import com.test.salesportal.model.items.attributes.ClassAttributes;
import com.test.salesportal.model.items.base.BaseItem;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.model.items.sports.DownhillSki;
import com.test.salesportal.model.items.sports.Ski;
import com.test.salesportal.model.items.sports.Snowboard;
import com.test.salesportal.model.items.vehicular.Boat;
import com.test.salesportal.model.items.vehicular.Car;

public class SalesItemTypes implements ItemTypes {

	public static final ItemTypes INSTANCE = new SalesItemTypes();
	
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
		typeNames = types.stream().map(t -> ItemTypes.getTypeName(t)).collect(Collectors.toList());
		typesSet = Collections.unmodifiableSet(new HashSet<>(types));
		
		jaxbTypes = new ArrayList<>(baseTypes.size() + types.size());
		//jaxbTypes.addAll(baseTypes);
		jaxbTypes.addAll(types);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Item> [] getJAXBTypeClasses() {
		return types.toArray(new Class[types.size()]);
	}

	public static List<Class<? extends Item>> getBaseTypesList() {
		return baseTypes;
	}

	@Override
	public List<Class<? extends Item>> getAllTypesList() {
		return types;
	}

	@Override
	public List<TypeInfo> getAllTypeInfosList() {
		return types.stream()
				.map(type -> getTypeInfo(type))
				.collect(Collectors.toList());
	}

	@Override
	public Set<Class<? extends Item>> getAllTypesSet() {
		return typesSet;
	}

	@Override
	public String [] getTypeNames() {
		return typeNames.toArray(new String[typeNames.size()]);
	}

	// TODO get from annotation
	@Override
	public String getTypeDisplayName(Class<? extends Item> type) {
		return getTypeInfo(type).getFacetDisplayName();
	}
	
	@Override
	public TypeInfo getTypeInfo(Class<? extends Item> type) {
		return getTypeByName(ItemTypes.getTypeName(type));
	}
	
	@Override
	public Set<ItemAttribute> getFacetAttributes(Collection<Class<? extends Item>> types) {
		final Set<ItemAttribute> facetAttributes = new HashSet<>();

		getAttributes(types, facetAttributes, a -> a.isFaceted(), a -> a);
		
		return facetAttributes;
	}

	@Override
	public Set<DistinctAttribute> getFreetextAttributes(Collection<Class<? extends Item>> types) {
		final Set<DistinctAttribute> attributes = new HashSet<>();

		getAttributes(types, attributes, a -> a.isFreetext(), a -> new DistinctAttribute(a));
		
		return attributes;
	}

	private static <T> void getAttributes(Collection<Class<? extends Item>> types, Collection<T> collection, Predicate<ItemAttribute> filter, Function<ItemAttribute, T> convert) {

		for (Class<? extends Item> type : types) {
			final TypeInfo typeInfo = INSTANCE.getTypeInfo(type);
			
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
			final String typeName = ItemTypes.getTypeName(type);
			
			if (typesByName.containsKey(typeName)) {
				throw new IllegalStateException("Already contains type with name " + typeName);
			}
			
			final ClassAttributes attributes = ClassAttributes.getFromClass(type);
			
			final FacetEntity facetEntity = type.getAnnotation(FacetEntity.class);
			
			final String facetTypeDisplayName = facetEntity != null ? facetEntity.value() : type.getSimpleName();
			
			typesByName.put(typeName, new TypeInfo(type, facetTypeDisplayName, attributes));
		}
	}
	
	@Override
	public TypeInfo getTypeByName(String typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException("typeName == null");
		}

		return typesByName.get(typeName);
	}

}
