package com.test.salesportal.rest.search.util;

import java.util.ArrayList;
import java.util.List;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;

public class SearchTypesUtil {

	public static List<Class<? extends Item>> computeTypes(String [] types) {
		
		if (types == null) {
			// No types selected, ought to return empty resultset
			types = new String[0];  
		}
		else if (types.length == 1 && types[0].equals("_all_")) {
			// Hack to get all types at the start and separate this case from the "no types" case above
			types = ItemTypes.getTypeNames();
		}
		
		
		final List<Class<? extends Item>> typesList = new ArrayList<>(types.length);
		
		for (String typeName : types) {
			final TypeInfo typeInfo = ItemTypes.getTypeByName(typeName);
			
			if (typeInfo == null) {
				throw new IllegalArgumentException("Unknown type " + typeName);
			}

			typesList.add(typeInfo.getType());
		}
	
		return typesList;
	}
}
