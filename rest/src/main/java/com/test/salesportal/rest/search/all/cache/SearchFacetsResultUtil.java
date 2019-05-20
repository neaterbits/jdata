package com.test.salesportal.rest.search.all.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.test.salesportal.common.CollectionUtil;
import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.attributes.ClassAttributes;
import com.test.salesportal.model.items.attributes.facets.FacetedAttributeDecimalRange;
import com.test.salesportal.model.items.attributes.facets.FacetedAttributeIntegerRange;
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

	static void addItem(SearchFacetsResult facetsResult, Item item, TypeInfo itemTypeInfo, List<TypeInfo> allTypes) {
		
		if (!item.getClass().equals(itemTypeInfo.getType())) {
			throw new IllegalArgumentException();
		}
		
		List<SearchFacetedTypeResult> types = facetsResult.getTypes();
		
		if (types == null) {
			types = new ArrayList<>(allTypes.size());
			facetsResult.setTypes(types);
		}
		
		// Add any missing types
		if (types.size() < allTypes.size()) {
		
			final List<SearchFacetedTypeResult> toAdd = new ArrayList<>(allTypes.size());
			
			for (TypeInfo typeInfo : allTypes) {
				if (!CollectionUtil.has(types, t -> typeInfo.getTypeName().equals(t.getType()))) {
					toAdd.add(new SearchFacetedTypeResult(typeInfo.getTypeName(), typeInfo.getFacetDisplayName()));
				}
			}
			
			types.addAll(toAdd);
		}

		final SearchFacetedTypeResult typeResult = CollectionUtil.find(types, t -> t.getType().equals(itemTypeInfo.getTypeName()));

		final List<SearchFacetedAttributeResult> facetAttributes = typeResult.getAttributes();
		
		makeAttributes(item, facetAttributes, typeResult::setAttributes, itemTypeInfo.getAttributes(), null);
	}
	
	private static boolean matchesSuperAttributeOrIsRootAttribute(ItemAttribute attribute, ItemAttribute superAttribute) {
		
		final boolean matches;
		
		if (superAttribute == null && attribute.getFacetSuperAttribute() == null) {
			matches = true;
		}
		else if (superAttribute != null && attribute.getFacetSuperAttribute() == null) {
			matches = false;
		}
		else if (superAttribute == null && attribute.getFacetSuperAttribute() != null) {
			matches = false;
		}
		else if (superAttribute != null && attribute.getFacetSuperAttribute() != null) {
			matches = superAttribute.getName().equals(attribute.getFacetSuperAttribute());
		}
		else {
			throw new IllegalStateException();
		}

		return matches;
	}
	
	private static void makeAttributes(
			Item item,
			List<SearchFacetedAttributeResult> facetAttributes,
			Consumer<List<SearchFacetedAttributeResult>> setFacetAttributes,
			ClassAttributes attributes,
			ItemAttribute superAttribute) {
		
		for (ItemAttribute attribute : attributes.asSet()) {
			
			if (!attribute.isFaceted()) {
				continue;
			}
				
			if (!matchesSuperAttributeOrIsRootAttribute(attribute, superAttribute)) {
				continue;
			}
			
			if (facetAttributes == null) {
				facetAttributes = new ArrayList<>();
				
				if (setFacetAttributes != null) {
					setFacetAttributes.accept(facetAttributes);
				}
			}
				
			final Object attributeValue = attribute.getObjectValue(item);
			
			SearchFacetedAttributeResult facetedAttributeResult = CollectionUtil.find(
					facetAttributes,
					facetAttribute -> facetAttribute.getId().equals(attribute.getName()));
			
			if (facetedAttributeResult == null) {
				
				final String attrId = attribute.getName();
				final String displayName = attribute.getFacetDisplayName();
				
				facetedAttributeResult = attribute.isSingleValue()
						? new SearchSingleValueFacetedAttributeResult(attrId, displayName, null)
						: new SearchRangeFacetedAttributeResult(attrId, displayName, null);
						
				facetAttributes.add(facetedAttributeResult);
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

					final Supplier<SearchSingleValueFacet> createNewValue = () -> new SearchSingleValueFacet(attributeValue, 1);
					
					SearchSingleValueFacet singleValueFacet;
					if (facets == null) {
						facets = new ArrayList<>();
						
						singleValueFacet = createNewValue.get();
						facets.add(singleValueFacet);
						singleValue.setValues(facets);
					}
					else {
						singleValueFacet = CollectionUtil.find(facets, f -> f.getValue().equals(attributeValue));
						
						if (singleValueFacet == null) {
							singleValueFacet = createNewValue.get();
							facets.add(singleValueFacet);
						}
						else {
							singleValueFacet.setMatchCount(singleValueFacet.getMatchCount() + 1);
						}
					}

					makeAttributes(
							item,
							singleValueFacet.getSubAttributes(),
							singleValueFacet::setSubAttributes,
							attributes,
							attribute);
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
				
				if (!matchesSuperAttributeOrIsRootAttribute(attribute, superAttribute)) {
					continue;
				}
				
				final Object attributeValue = attribute.getObjectValue(item);
				
				final SearchFacetedAttributeResult facetedAttributeResult = CollectionUtil.find(
						facetAttributes,
						facetAttribute -> facetAttribute.getId().equals(attribute.getName()));
				
				if (facetedAttributeResult == null) {
					continue;
				}
				
				if (attributeValue == null) {
					
					final Integer noAttributeValueCount = facetedAttributeResult.getNoAttributeValueCount();
					
					if (noAttributeValueCount != null) {
						
						if (noAttributeValueCount <= 0) {
							throw new IllegalStateException();
						}
						
						facetedAttributeResult.setNoAttributeValueCount(noAttributeValueCount == 1 ? null : noAttributeValueCount - 1);
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
								
								if (singleValueFacet.getMatchCount() == 1) {
									singleResult.getValues().removeIf(value -> value.getValue().equals(attributeValue));
								}
								else {
									singleValueFacet.setMatchCount(matchCount - 1);
								}
								
								if (singleValueFacet.getSubAttributes() != null) {
									subtractMatchCountForAttributes(item, singleValueFacet.getSubAttributes(), attributes, attribute);
								}
							}
							
							if (singleResult.getValues().isEmpty()) {
								singleResult.setValues(null);
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
