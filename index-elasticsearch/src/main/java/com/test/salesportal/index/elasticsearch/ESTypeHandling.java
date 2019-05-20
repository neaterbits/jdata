package com.test.salesportal.index.elasticsearch;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;

// Base class for various handling of ES types
public abstract class ESTypeHandling {

	// Filter down to ES types that exist in index
	abstract Set<String> getESIndexTypes(Collection<Class<? extends Item>> allTypes);
	
	final Set<String> getCreateIndexTypes(Collection<Class<? extends Item>> allTypes) {
		return getESIndexTypes(allTypes);
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

	abstract boolean hasQueryTypeFilter();
	
	abstract QueryBuilder createQueryTypeFilter(Class<? extends Item> type);
	
	abstract boolean hasAggregationTypeFilter();
	
	abstract AggregationBuilder createAggregationTypeFilter(Class<? extends Item> type);
}
