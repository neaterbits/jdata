package com.test.salesportal.model.items.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.test.salesportal.model.items.DistinctAttribute;
import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.TypeInfo;

public interface ItemTypes {

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

	public static String getTypeName(Class<? extends Item> type) {
		return type.getSimpleName();
	}

	public static String getTypeName(Item item) {
		return getTypeName(getType(item));
	}

	public static Class<? extends Item> getType(Item item) {
		return item.getClass();
	}
	
	String [] getTypeNames();
	
	TypeInfo getTypeByName(String typeName);
	
	TypeInfo getTypeInfo(Class<? extends Item> type);
	
	default TypeInfo getTypeInfo(Item item) {
		return getTypeInfo(getType(item));
	}

	List<Class<? extends Item>> getAllTypesList();
	
	Set<Class<? extends Item>> getAllTypesSet();

	List<TypeInfo> getAllTypeInfosList();

	String getTypeDisplayName(Class<? extends Item> type);

	Set<DistinctAttribute> getFreetextAttributes(Collection<Class<? extends Item>> types);

	Set<ItemAttribute> getFacetAttributes(Collection<Class<? extends Item>> types);

	Class<? extends Item> [] getJAXBTypeClasses();
}
