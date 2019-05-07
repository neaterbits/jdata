package com.test.salesportal.rest.search.all.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.test.salesportal.common.CollectionUtil;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.attributes.ClassAttributes;
import com.test.salesportal.model.attributes.facets.FacetedAttributeDecimalRange;
import com.test.salesportal.model.attributes.facets.FacetedAttributeIntegerRange;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeDecimalRangeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeIntegerRangeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeRangeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedTypeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;
import com.test.salesportal.rest.search.model.facetresult.SearchRangeFacetedAttributeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacet;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacetedAttributeResult;

class SearchFacetsResultUtil {

	static void addItem(SearchFacetsResult facetsResult, Item item, TypeInfo itemTypeInfo) {
		
		if (!item.getClass().equals(itemTypeInfo.getType())) {
			throw new IllegalArgumentException();
		}
		
		List<SearchFacetedTypeResult> types = facetsResult.getTypes();
		
		SearchFacetedTypeResult typeResult;
		
		if (types == null) {
			types = new ArrayList<>();
			facetsResult.setTypes(types);
		
			typeResult = null;
		}
		else {
			typeResult = CollectionUtil.find(types, t -> t.getType().equals(itemTypeInfo.getTypeName()));
		}
		
		if (typeResult == null) {
			typeResult = new SearchFacetedTypeResult(
					itemTypeInfo.getTypeName(),
					itemTypeInfo.getFacetDisplayName());
			
			types.add(typeResult);
		}

		List<SearchFacetedAttributeResult> facetAttributes = typeResult.getAttributes();
		
		if (facetAttributes == null) {
			facetAttributes = new ArrayList<>();
			typeResult.setAttributes(facetAttributes);
		}
		
		makeAttributes(item, facetAttributes, itemTypeInfo.getAttributes(), null);
	}
	
	private static void makeAttributes(
			Item item,
			List<SearchFacetedAttributeResult> facetAttributes,
			ClassAttributes attributes,
			ItemAttribute superAttribute) {
		
		for (ItemAttribute attribute : attributes.asSet()) {
			
			if (attribute.isFaceted()) {
				
				if (superAttribute != null && !superAttribute.getName().equals(attribute.getFacetSuperAttribute())) {
					continue;
				}
				
				final Object attributeValue = attribute.getObjectValue(item);
				
				SearchFacetedAttributeResult facetedAttributeResult = CollectionUtil.find(
						facetAttributes,
						facetAttribute -> facetAttribute.getName().equals(attribute.getName()));
				
				if (facetedAttributeResult == null) {
					facetedAttributeResult = attribute.isSingleValue()
							? new SearchSingleValueFacetedAttributeResult()
							: new SearchRangeFacetedAttributeResult();
				}
				
				if (attributeValue == null) {
					final int noAttributeValueCount = facetedAttributeResult.getNoAttributeValueCount() != null
							? facetedAttributeResult.getNoAttributeValueCount()
							: 0;
					
					facetedAttributeResult.setNoAttributeValueCount(noAttributeValueCount + 1);
				}
				else {
					
					if (attribute.isSingleValue()) {
						final SearchSingleValueFacetedAttributeResult singleValue = (SearchSingleValueFacetedAttributeResult)facetedAttributeResult;
					
						List<SearchSingleValueFacet> facets = singleValue.getValues();

						final Supplier<SearchSingleValueFacet> createNewValue = () -> {

							final List<SearchFacetedAttributeResult> list = new ArrayList<>();
							
							makeAttributes(item, list, attributes, attribute);
							 
							final SearchFacetedAttributeResult [] subAttributes = list.toArray(new SearchFacetedAttributeResult[list.size()]);

							return new SearchSingleValueFacet(attributeValue, 1, subAttributes);
						};
						
						if (facets == null) {
							facets = new ArrayList<>();
							
							facets.add(createNewValue.get());
							singleValue.setValues(facets);
						}
						else {
							
							final SearchSingleValueFacet singleValueFacet = CollectionUtil.find(facets, f -> f.getValue().equals(attributeValue));
							
							if (singleValueFacet == null) {
								facets.add(createNewValue.get());
							}
							else {
								singleValueFacet.setMatchCount(singleValueFacet.getMatchCount() + 1);
							}
						}
					}
					else if (attribute.isRange()) {
						
						final SearchRangeFacetedAttributeResult rangeAttributeResult = (SearchRangeFacetedAttributeResult)facetedAttributeResult;
						
						List<SearchFacetedAttributeRangeResult<?>> ranges = rangeAttributeResult.getRanges();
						
						if (ranges == null) {
							// Add all ranges
							ranges = new ArrayList<>(attribute.getRangeCount());
							
							switch (attribute.getAttributeType()) {
							case INTEGER:
								for (FacetedAttributeIntegerRange integerRange : attribute.getIntegerRanges()) {
									ranges.add(new SearchFacetedAttributeIntegerRangeResult(integerRange.getLower(), integerRange.getUpper(), 0));
								}
								break;
								
							case DECIMAL:
								for (FacetedAttributeDecimalRange decimalRange : attribute.getDecimalRanges()) {
									ranges.add(new SearchFacetedAttributeDecimalRangeResult(decimalRange.getLower(), decimalRange.getUpper(), 0));
								}
								break;
								
							default:
								throw new UnsupportedOperationException();
							}
							
							rangeAttributeResult.setRanges(ranges);
						}

						// Find the range for this attribute value
						for (SearchFacetedAttributeRangeResult<?> range : ranges) {
							
							if (SearchRangeUtil.matches(attributeValue, attribute, range.getLower(), true, range.getUpper(), false)) {
								range.setMatchCount(range.getMatchCount() + 1);
								break;
							}
						}
					}
					else {
						throw new UnsupportedOperationException();
					}
				}
			}
		}
		
	}
	
	static void deleteItem(SearchFacetsResult facetsResult, Item item, TypeInfo itemTypeInfo) {

		final SearchFacetedTypeResult typeResult = CollectionUtil.find(
				facetsResult.getTypes(),
				t -> t.getType().equals(itemTypeInfo.getTypeName()));
		
		if (typeResult != null) {
			subtractMatchCountForAttributes(item, typeResult.getAttributes(), itemTypeInfo.getAttributes(), null);
		}
	}
	
	private static void subtractMatchCountForAttributes(
			Item item,
			List<SearchFacetedAttributeResult> facetAttributes,
			ClassAttributes attributes,
			ItemAttribute superAttribute) {
	
		if (facetAttributes != null && !facetAttributes.isEmpty()) {
			
			for (ItemAttribute attribute : attributes.asSet()) {
				
				if (!attribute.isFaceted()) {
					continue;
				}
				
				if (superAttribute != null && !superAttribute.getName().equals(attribute.getFacetSuperAttribute())) {
					continue;
				}

				
				final Object attributeValue = attribute.getObjectValue(item);
				
				final SearchFacetedAttributeResult facetedAttributeResult = CollectionUtil.find(
						facetAttributes,
						facetAttribute -> facetAttribute.getName().equals(attribute.getName()));
				
				if (facetedAttributeResult == null) {
					continue;
				}
				
				if (attributeValue == null) {
					
					final Integer noAttributeValueCount = facetedAttributeResult.getNoAttributeValueCount();
					
					if (noAttributeValueCount != null) {
						
						if (noAttributeValueCount <= 0) {
							throw new IllegalStateException();
						}
						
						facetedAttributeResult.setNoAttributeValueCount(noAttributeValueCount - 1);
					}
				}
				else {
				
					if (attribute.isSingleValue()) {
						final SearchSingleValueFacetedAttributeResult singleResult = (SearchSingleValueFacetedAttributeResult)facetedAttributeResult;
						
						if (singleResult.getValues() != null) {
							final SearchSingleValueFacet singleValueFacet = CollectionUtil.find(singleResult.getValues(), f -> f.getValue().equals(attributeValue));
						
							if (singleValueFacet != null) {
								final int matchCount = singleValueFacet.getMatchCount();
								
								if (matchCount <= 0) {
									throw new IllegalStateException();
								}
								
								singleValueFacet.setMatchCount(matchCount - 1);
								
								if (singleValueFacet.getSubAttributes() != null) {
									subtractMatchCountForAttributes(item, singleValueFacet.getSubAttributes(), attributes, attribute);
								}
							}
						}
					}
					else if (attribute.isRange()) {
						final SearchRangeFacetedAttributeResult rangeResult = (SearchRangeFacetedAttributeResult)facetedAttributeResult;
						
						for (SearchFacetedAttributeRangeResult<?> range : rangeResult.getRanges()) {
							
							if (SearchRangeUtil.matches(attributeValue, attribute, range.getLower(), true, range.getUpper(), false)) {
								
								final int matchCount = range.getMatchCount();
								
								if (matchCount <= 0) {
									throw new IllegalStateException();
								}
								
								range.setMatchCount(matchCount - 1);
								break;
							}
						}
					}
					else {
						throw new UnsupportedOperationException();
					}
				}
			}
		}
	}
}
