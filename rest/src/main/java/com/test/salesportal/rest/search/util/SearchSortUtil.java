package com.test.salesportal.rest.search.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.test.salesportal.common.StringUtil;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.model.SortAttributeAndOrder;
import com.test.salesportal.model.SortOrder;
import com.test.salesportal.model.annotations.SortableType;
import com.test.salesportal.model.attributes.ClassAttributes;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.rest.search.model.sorting.SearchSortOrderAlternative;

public class SearchSortUtil {
	
	public static final String ASCENDING = "ascending";
	public static final String DESCENDING = "descending";

	public static List<SortAttributeAndOrder> decodeSortOrders(String [] sortOrder, List<Class<? extends Item>> typesList) {
		final List<SortAttributeAndOrder> sortAttributes;
		if (sortOrder != null) {
			final Set<Class<? extends Item>> baseTypes = ItemTypes.getBaseTypes(typesList);

			final Set<Class<? extends Item>> sortOrderTypes = new HashSet<>(baseTypes.size() + typesList.size());

			// Must add base types to sortorder types since some attributes are only in base types
			sortOrderTypes.addAll(baseTypes);
			sortOrderTypes.addAll(typesList);
			
			sortAttributes = decodeSortOrders(sortOrderTypes, sortOrder);
		}
		else {
			// Get sort order from common denominator among types
			sortAttributes = SearchSortUtil.computeAndSortPossibleSortAttributes(typesList)
					.stream().map(a -> new SortAttributeAndOrder(a, SortOrder.ASCENDING))
					.collect(Collectors.toList());
		}
	
		return sortAttributes;
	}
	
	public static SearchSortOrderAlternative [] computeAndSortPossibleSortOrders(Collection<Class<? extends Item>> types) {
		
		final List<SortAttribute> attributes = computeAndSortPossibleSortAttributes(types);

		// Convert to sort orders
		return getSortOrdersFromAttributes(attributes);
	}

	
	private static SearchSortOrderAlternative [] getSortOrdersFromAttributes(List<SortAttribute> attributes) {
		
		final List<SearchSortOrderAlternative> order = new ArrayList<>();

		attributes.forEach(attr -> {
			final SortableType sortableType = attr.getSortableType();
			
			final String sortOrderName = attr.encodeToString();
			final String sortOrderDisplayName = attr.getSortableTitle();
			
			if (sortableType == SortableType.NUMERICAL || sortableType == SortableType.TIME) {
				order.add(new SearchSortOrderAlternative(sortOrderName + '_' + ASCENDING, sortOrderDisplayName + " - low to high"));
				order.add(new SearchSortOrderAlternative(sortOrderName + '_' + DESCENDING, sortOrderDisplayName + " - high to low"));
			}
			else {
				order.add(new SearchSortOrderAlternative(sortOrderName, sortOrderDisplayName));
			}
		});
		
		final SearchSortOrderAlternative [] array;
		
		if (order.isEmpty()) {
			array = new SearchSortOrderAlternative[0];
		}
		else {
			array = order.toArray(new SearchSortOrderAlternative[order.size()]);
		}

		return array;
	}

	// Need to hash on declaring-class for attribute
	// so that base class attributes are only counted once no matter the subclass (eg 'Title' is in baseclass
	// for both Car and Snowboard)
	private static List<SortAttribute> computeAndSortPossibleSortAttributes(Collection<Class<? extends Item>> types) {

		final List<SortAttribute> attributes;
		
		if (types.isEmpty()) {
			attributes = Collections.emptyList();
		}
		else if (types.size() == 1) {
			 attributes = getSortableAttributesFromType(types.iterator().next()).stream()
					 .map(a -> a.makeSortAttribute())
					 .collect(Collectors.toList());
		}
		else {
		
			// Only return those that are common to all types?
			final Set<SortAttribute> commonSortableAttributes = new HashSet<>();

			for (Class<? extends Item> type : types) {
				final List<SortAttribute> typeAttributes = getSortableAttributesFromType(type).stream()
						.map(a -> a.makeSortAttribute())
						.collect(Collectors.toList());
				
				if (commonSortableAttributes.isEmpty()) {
					commonSortableAttributes.addAll(typeAttributes);
				}
				else {
					commonSortableAttributes.retainAll(typeAttributes);
				}
			}

			attributes = new ArrayList<>(commonSortableAttributes);
		}

		if (attributes.size() > 1) {
			Collections.sort(attributes, SortAttribute.SORTABLE_PRIORITY_COMPARATOR);
		}
		
		return attributes;
	}

	private static List<ItemAttribute> getSortableAttributesFromType(Class<? extends Item> type) {
		
		final ClassAttributes attrs = ItemTypes.getTypeInfo(type).getAttributes();

		final List<ItemAttribute> sortAttributes = new ArrayList<>();

		attrs.forEach(attr -> {
			if (attr.isSortable()) {
				sortAttributes.add(attr);
			}
				
		});
		
		return sortAttributes;
	}

	private static List<SortAttributeAndOrder> decodeSortOrders(Collection<Class<? extends Item>> types, String [] sortOrders) {
		
		final List<SortAttributeAndOrder> result = new ArrayList<>(sortOrders.length);
		
		for (String sortOrder : sortOrders) {
			final String [] parts = StringUtil.split(sortOrder, '_');

			final SortAttributeAndOrder attributeAndOrder;
			
			if (parts.length == 1) {
				// Only classname:attrname
				final SortAttribute attribute = SortAttribute.decode(types, parts[0]);
			
				attributeAndOrder = new SortAttributeAndOrder(attribute, SortOrder.ASCENDING);
			}
			else if (parts.length == 2) {
				// sort order specified as well
				final SortAttribute attribute = SortAttribute.decode(types, parts[0]);
				
				final SortOrder sortOrderEnum;
				
				switch (parts[1]) {
				case ASCENDING:
					sortOrderEnum = SortOrder.ASCENDING;
					break;
					
				case DESCENDING:
					sortOrderEnum = SortOrder.DESCENDING;
					break;
					
				default:
					throw new IllegalArgumentException("Unknown sort order " + parts[1]);
				}

				attributeAndOrder = new SortAttributeAndOrder(attribute, sortOrderEnum);
			}
			else {
				throw new IllegalArgumentException("Unable to parse sort order " + sortOrder);
			}
			
			result.add(attributeAndOrder);
		}

		return result;
	}

}
