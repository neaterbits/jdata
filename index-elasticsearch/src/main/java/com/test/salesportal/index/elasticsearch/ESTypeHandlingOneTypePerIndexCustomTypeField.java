package com.test.salesportal.index.elasticsearch;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.base.ItemTypes;

/**
 * Store all types in one index, merge all attributes together.
 * Must have a custom type-field in all items so that we can filter on this
 */

public class ESTypeHandlingOneTypePerIndexCustomTypeField extends ESTypeHandling {

	private static final String ITEM_TYPE = ItemTypes.getTypeName(Item.class);
	
	private static final String TYPE_FIELD = "item_type";
	
	private static final Set<String> TYPES;
	
	static {
		final Set<String> types = new HashSet<>(1);
		
		types.add(ITEM_TYPE);
		
		TYPES = Collections.unmodifiableSet(types);
	}
	
	private final ItemTypes itemTypes;
	

	public ESTypeHandlingOneTypePerIndexCustomTypeField(ItemTypes itemTypes) {

		if (itemTypes == null) {
			throw new IllegalArgumentException("itemTypes == null");
		}
		
		this.itemTypes = itemTypes;
	}

	@Override
	Set<String> getESIndexTypes(Collection<Class<? extends Item>> allTypes) {
		return TYPES;
	}

	@Override
	Set<ItemAttribute> getCreateIndexAttributes(String esType) {
		
		// Distinct attributes across all types
		final Set<ItemAttribute> distinctAttributes = new HashSet<>();

		for (String typeName : itemTypes.getTypeNames()) {
			final TypeInfo typeInfo = itemTypes.getTypeByName(typeName);
			
			typeInfo.getAttributes().forEach(a -> distinctAttributes.add(a));
		}
		
		return Collections.unmodifiableSet(distinctAttributes);
	}

	@Override
	String getESTypeName(Class<? extends Item> type) {
		return ITEM_TYPE;
	}

	
	@Override
	Map<String, String> createIndexCustomFields(String typeName) {

		final Map<String, String> map = new HashMap<>();

		map.put(TYPE_FIELD, "keyword");

		return map;
	}

	@Override
	Map<String, Object> indexCustomFields(Class<? extends Item> type) {
		
		final Map<String, Object> map = new HashMap<>();

		map.put(TYPE_FIELD, ItemTypes.getTypeName(type));
		
		return map;
	}

	@Override
	boolean hasQueryTypeFilter() {
		return true;
	}

	@Override
	QueryBuilder createQueryTypeFilter(Class<? extends Item> type) {
		final String typeName = ItemTypes.getTypeName(type);

		return QueryBuilders.termQuery(TYPE_FIELD, typeName);
	}

	@Override
	boolean hasAggregationTypeFilter() {
		return true;
	}

	@Override
	AggregationBuilder createAggregationTypeFilter(Class<? extends Item> type) {
		
		final String typeName = ItemTypes.getTypeName(type);
		
		return AggregationBuilders.filter(typeName + "_agg", QueryBuilders.termQuery(TYPE_FIELD, typeName));
	}
}
