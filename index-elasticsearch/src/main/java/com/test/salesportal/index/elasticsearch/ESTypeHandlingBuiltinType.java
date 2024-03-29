package com.test.salesportal.index.elasticsearch;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.base.ItemTypes;

/**
 * Store multiple types in one index.
 * This does not work anymore in >= 6.x but keep the code here for reference
 */

@Deprecated
public class ESTypeHandlingBuiltinType extends ESTypeHandling {

	private final ItemTypes itemTypes;
	
	public ESTypeHandlingBuiltinType(ItemTypes itemTypes) {

		if (itemTypes == null) {
			throw new IllegalArgumentException("itemTypes == null");
		}
		
		this.itemTypes = itemTypes;
	}

	@Override
	Set<String> getESIndexTypes(Collection<Class<? extends Item>> allTypes) {
		
		// Return all types since we use multiple types per index
		return allTypes.stream().map(type -> ItemTypes.getTypeName(type)).collect(Collectors.toSet());
	}

	@Override
	Set<ItemAttribute> getCreateIndexAttributes(String esType) {
		final Set<ItemAttribute> attributes = new HashSet<>();
		
		itemTypes.getTypeByName(esType).getAttributes().forEach(a -> attributes.add(a));
		
		return Collections.unmodifiableSet(attributes);
	}

	@Override
	String getESTypeName(Class<? extends Item> type) {
		return ItemTypes.getTypeName(type);
	}

	@Override
	Map<String, String> createIndexCustomFields(String typeName) {
		return null;
	}

	@Override
	Map<String, Object> indexCustomFields(Class<? extends Item> type) {
		return null;
	}

	@Override
	boolean hasQueryTypeFilter() {
		return true;
	}

	@Override
	QueryBuilder createQueryTypeFilter(Class<? extends Item> type) {
		final String typeName = getESTypeName(type);
		
		return QueryBuilders.typeQuery(typeName);
	}

	@Override
	boolean hasAggregationTypeFilter() {
		return true;
	}

	@Override
	AggregationBuilder createAggregationTypeFilter(Class<? extends Item> type) {
		final String typeName = getESTypeName(type);
		
		return AggregationBuilders.filter(typeName + "_agg", QueryBuilders.typeQuery(typeName));
	}
}
