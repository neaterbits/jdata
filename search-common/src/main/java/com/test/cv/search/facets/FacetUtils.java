package com.test.cv.search.facets;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.test.cv.model.AttributeEnum;
import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.attributes.facets.FacetedAttributeComparableRange;
import com.test.cv.model.items.ItemTypes;

public class FacetUtils {
	public interface FacetFunctions<I, F> {
		boolean isType(I item, String typeName);
		
		F getField(I item, String fieldName);
		
		Integer getIntegerValue(F field);
		
		BigDecimal getDecimalValue(F field);
		
		<T extends Enum<T>> T getEnumValue(Class<T> enumClass, F field);
		
		Boolean getBooleanValue(F field);
		
		Object getObjectValue(ItemAttribute attribute, F field);
	}

	public static <I, F> ItemsFacets computeFacets(List<I> documents, Set<ItemAttribute> facetedAttributes, FacetFunctions<I, F> functions) {
		
		// Sort by type of item
		final Set<Class<? extends Item>> distinctTypes =
				facetedAttributes.stream()
					.map(attribute -> attribute.getItemType())
					.collect(Collectors.toSet());
		
		final List<TypeFacets> typeFacets = new ArrayList<>(distinctTypes.size());
		
		for (Class<? extends Item> itemType : distinctTypes) {
		
			final List<ItemAttribute> typeAttributes = facetedAttributes.stream()
				.filter(attribute -> attribute.getItemType().equals(itemType))
				.collect(Collectors.toList());
			
			final String typeName = ItemTypes.getTypeName(itemType);
			
			final List<I> typeDocuments = documents.stream()
					.filter(item -> functions.isType(item, typeName))
					.collect(Collectors.toList());
			
			final TypeFacets tf = computeFacetsForType(itemType, typeDocuments, typeAttributes, functions);
			
			typeFacets.add(tf);
		}
		
		final ItemsFacets itemsFacets = new ItemsFacets(typeFacets);
		
		return itemsFacets;
	}

	
	private static <I, F> TypeFacets computeFacetsForType(Class<? extends Item> itemType, List<I> documents, List<ItemAttribute> typeAttributes, FacetFunctions<I, F> functions) {
		
		final Map<ItemAttribute, IndexFacetedAttributeResult> attributeResults = new HashMap<>(typeAttributes.size());
		//final List<IndexFacetedAttributeResult> attributeResults = new ArrayList<>(typeAttributes.size());
		
		for (I d : documents) {
			for (ItemAttribute attribute : typeAttributes) {
				final F field = functions.getField(d, attribute.getName());
				
				if (field == null) {
					continue;
				}
				
				// Get the field value from document
				if (attribute.isFaceted()) {
					if (attribute.getIntegerRanges() != null) {
						
						// Find which range we are in
						final int value = functions.getIntegerValue(field);

						computeFacetsForRange(attribute, attribute.getIntegerRanges(), value, attributeResults);
					}
					else if (attribute.getDecimalRanges() != null) {
						
						// Find which range we are in
						final BigDecimal value = functions.getDecimalValue(field);
						
						computeFacetsForRange(attribute, attribute.getDecimalRanges(), value, attributeResults);
					}
					else {
						// Single-value
						IndexSingleValueFacetedAttributeResult singleValueResult = (IndexSingleValueFacetedAttributeResult)attributeResults.get(attribute);
						
						// 
						
						// TODO avoid instantiation?
						// TODO subfacets
						if (singleValueResult == null) {
							singleValueResult = new IndexSingleValueFacetedAttributeResult(attribute, new TreeMap<>());
							attributeResults.put(attribute, singleValueResult);
						}
						
						final Object value = functions.getObjectValue(attribute, field);
						
						if (value != null) {
							IndexSingleValueFacet valueFacet = singleValueResult.getForValue(value);
							
							if (valueFacet == null) {
								
								final Object displayValue;
								
								if (value instanceof AttributeEnum) {
									displayValue = ((AttributeEnum)value).getDisplayName();
								}
								else {
									displayValue = value;
								}
								
								valueFacet = new IndexSingleValueFacet(value, displayValue, null);
								
								singleValueResult.putForValue(value, valueFacet);
							}
							
							valueFacet.increaseMatchCount();
						}
						
						// Find or add corresponding value
					}
				}
			}
		}

		final TypeFacets typeFacets = new TypeFacets(itemType, new ArrayList<>(attributeResults.values()));
		
		return typeFacets;
	}


	private static <T extends Comparable<T>, R extends FacetedAttributeComparableRange<T>> void computeFacetsForRange(
			ItemAttribute attribute,
			R [] ranges,
			T value,
			Map<ItemAttribute, IndexFacetedAttributeResult> attributeResults) {
		
		final IndexRangeFacetedAttributeResult rangeResult
				= getOrAddRange(attributeResults, attribute, ranges.length);

		boolean found = false;
		for (int i = 0; i < ranges.length; ++ i) {
			final R range = ranges[i];

			if (range.getLower() == null && range.getUpper() == null) {
				found = true;
			}
			else if (range.getLower() == null) {
				if (value.compareTo(range.getUpper()) <= 0) {
					found = true;
				}
			}
			else if (range.getUpper() == null) {
				if (range.getLower().compareTo(value) <= 0 ) {
					found = true;
				}
			}
			else if (range.getLower().compareTo(value) <= 0 && value.compareTo(range.getUpper()) <= 0) {
				found = true;
			}
			
			if (found) {
				++ rangeResult.getMatchCounts()[i];
				break;
			}
		}
		
		if (!found) {
			throw new IllegalStateException("Unable to find range for value for decimal attribute " + attribute.getName());
		}
		
	}
	
	private static IndexRangeFacetedAttributeResult getOrAddRange(Map<ItemAttribute, IndexFacetedAttributeResult> attributeResults, ItemAttribute attribute, int count) {
		IndexRangeFacetedAttributeResult rangeResult = (IndexRangeFacetedAttributeResult)attributeResults.get(attribute);
		
		if (rangeResult == null) {
			rangeResult = new IndexRangeFacetedAttributeResult(attribute, new int[count]);
			
			attributeResults.put(attribute, rangeResult);
		}
		
		return rangeResult;
	}
}
