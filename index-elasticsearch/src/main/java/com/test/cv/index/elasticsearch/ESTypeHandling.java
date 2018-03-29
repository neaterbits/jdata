package com.test.cv.index.elasticsearch;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.search.aggregations.AggregationBuilder;

import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;

// Base class for various handling of ES types
public abstract class ESTypeHandling {

	// Filter down to ES types that exist in index
	abstract Set<String> getESIndexTypes(Collection<Class<? extends Item>> allTypes);
	
	final Set<String> getCreateIndexTypes(Class<? extends Item> [] allTypes) {
		return getESIndexTypes(Arrays.asList(allTypes));
	}
	
	// Get all attributes that belong to an ES type
	abstract Set<ItemAttribute> getCreateIndexAttributes(String esType);
	
	/**
	 * Extra field types to add to index
	 * 
	 * @param type item type
	 * 
	 * @return map from field name to field type
	 */
	abstract Map<String, String> createIndexCustomFields(String typeName);

	/**
	 * Extra fields to add to indexed item
	 * 
	 * @param type item type
	 * 
	 * @return map from field name to field
	 */
	
	abstract Map<String, Object> indexCustomFields(Class<? extends Item> type);
	
	abstract String getESTypeName(Class<? extends Item> type);
	
	abstract boolean hasTypeFilter();
	
	abstract AggregationBuilder createTypeFilter(Class<? extends Item> type);
}
