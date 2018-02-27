package com.test.cv.model.items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.test.cv.model.Item;
import com.test.cv.model.attributes.ClassAttributes;

public class ItemTypes {
	
	
	private static final List<Class<? extends Item>> types = Arrays.asList(
			Snowboard.class,
			Ski.class,
			Item.class);
	
	private static final Map<String, TypeInfo> typesByName;
	
	public static String getTypeName(Class<? extends Item> type) {
		return type.getSimpleName();
	}

	// TODO get from annotation
	public static String getTypeDisplayName(Class<? extends Item> type) {
		return getTypeName(type);
	}
	
	static {
		typesByName = new HashMap<>();
		
		for (Class<? extends Item> type : types) {
			final String typeName = getTypeName(type);
			
			if (typesByName.containsKey(typeName)) {
				throw new IllegalStateException("Already contains type with name " + typeName);
			}
			
			final ClassAttributes attributes = ClassAttributes.getFromClass(type);
			
			typesByName.put(typeName, new TypeInfo(type, attributes));
		}
	}
	
	public static TypeInfo getTypeByName(String typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException("typeName == null");
		}

		return typesByName.get(typeName);
	}

}
