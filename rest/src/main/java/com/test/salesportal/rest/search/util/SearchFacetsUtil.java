package com.test.salesportal.rest.search.util;

import java.util.ArrayList;
import java.util.List;

import com.test.salesportal.model.FacetFiltering;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.attributes.AttributeType;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeDecimalRangeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeIntegerRangeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeRangeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedTypeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;
import com.test.salesportal.rest.search.model.facetresult.SearchInputFacetedAttributeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchRangeFacetedAttributeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacet;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacetedAttributeResult;
import com.test.salesportal.search.facets.IndexFacetedAttributeResult;
import com.test.salesportal.search.facets.IndexRangeFacetedAttributeResult;
import com.test.salesportal.search.facets.IndexSingleValueFacet;
import com.test.salesportal.search.facets.IndexSingleValueFacetedAttributeResult;
import com.test.salesportal.search.facets.ItemsFacets;
import com.test.salesportal.search.facets.TypeFacets;

public class SearchFacetsUtil {

	public static SearchFacetsResult convertFacets(ItemsFacets facets) {
		final SearchFacetsResult result = new SearchFacetsResult();
		
		final List<SearchFacetedTypeResult> typeFacetsResult = new ArrayList<>(facets.getTypes().size());
		
		for (TypeFacets typeFacet : facets.getTypes()) {
			
			final SearchFacetedTypeResult typeResult = new SearchFacetedTypeResult();

			final List<SearchFacetedAttributeResult> facetAttributesResult = convertAttributeList(typeFacet.getAttributes());
			
			typeResult.setType(getTypeId(typeFacet.getType()));
			typeResult.setDisplayName(getTypeDisplayName(typeFacet.getType()));
			typeResult.setAutoExpandAttributesCount(
					typeFacet.getAutoExpandAttributesCount() != 0
						? typeFacet.getAutoExpandAttributesCount()
						: null);
			typeResult.setAttributes(facetAttributesResult);
			
			typeFacetsResult.add(typeResult);
		}
		
		result.setTypes(typeFacetsResult);

		return result;
	}
	
	private static List<SearchFacetedAttributeResult> convertAttributeList(List<IndexFacetedAttributeResult> attributes) {

		final List<SearchFacetedAttributeResult> facetAttributesResult = new ArrayList<>(attributes.size());

		for (IndexFacetedAttributeResult indexFacetedAttribute : attributes) {
			final SearchFacetedAttributeResult searchFacetedAttribute;

			final FacetFiltering filtering = indexFacetedAttribute.getAttribute().getFacetFiltering();

			if (filtering == FacetFiltering.INPUT) {
				final SearchInputFacetedAttributeResult searchInputFacetedAttributeResult = new SearchInputFacetedAttributeResult();
				
				searchFacetedAttribute = searchInputFacetedAttributeResult;
			}
			else if (indexFacetedAttribute instanceof IndexSingleValueFacetedAttributeResult) {
				final IndexSingleValueFacetedAttributeResult indexSingleValueFacetedAttributeResult
						= (IndexSingleValueFacetedAttributeResult)indexFacetedAttribute;
				
				final SearchSingleValueFacetedAttributeResult searchSingleValueFacetedAttribute = new SearchSingleValueFacetedAttributeResult();
				
				final List<SearchSingleValueFacet> searchValues = new ArrayList<>(indexSingleValueFacetedAttributeResult.getValues().size());
				for (IndexSingleValueFacet indexValue : indexSingleValueFacetedAttributeResult.getValues()) {
					
					final SearchSingleValueFacet searchValue = new SearchSingleValueFacet();
					
					searchValue.setValue(indexValue.getValue());
					searchValue.setDisplayValue(indexValue.getDisplayValue());
					searchValue.setMatchCount(indexValue.getMatchCount());
					
					if (indexValue.getSubFacets() != null) {
						searchValue.setSubAttributes(convertAttributeList(indexValue.getSubFacets()));
					}
					
					searchValues.add(searchValue);
				}
				
				searchSingleValueFacetedAttribute.setValues(searchValues);

				searchFacetedAttribute = searchSingleValueFacetedAttribute;
			}
			else if (indexFacetedAttribute instanceof IndexRangeFacetedAttributeResult) {
				final IndexRangeFacetedAttributeResult indexRangeFacetedAttributeResult
						= (IndexRangeFacetedAttributeResult)indexFacetedAttribute;

				final ItemAttribute attribute = indexFacetedAttribute.getAttribute();
				final AttributeType attributeType = attribute.getAttributeType();
				
				final int [] matchCounts = indexRangeFacetedAttributeResult.getMatchCounts();
				
				final List<SearchFacetedAttributeRangeResult<?>> ranges = new ArrayList<>(matchCounts.length);
				
				// Convert match count for each range to REST response format
				// Response contains the ranges as well for ease of use from UI code
				switch (attributeType) {
				case INTEGER: {
					for (int i = 0; i < matchCounts.length; ++ i) {
						final SearchFacetedAttributeIntegerRangeResult searchRange = new SearchFacetedAttributeIntegerRangeResult();
						
						// Set range lower and upper
						searchRange.setLower(attribute.getIntegerRanges()[i].getLower());
						searchRange.setUpper(attribute.getIntegerRanges()[i].getUpper());
						searchRange.setMatchCount(matchCounts[i]);
						
						ranges.add(searchRange);
					}
					break;
				}
					
				case DECIMAL: {
					for (int i = 0; i < matchCounts.length; ++ i) {
						final SearchFacetedAttributeDecimalRangeResult searchRange = new SearchFacetedAttributeDecimalRangeResult();
						
						// Set range lower and upper
						searchRange.setLower(attribute.getDecimalRanges()[i].getLower());
						searchRange.setUpper(attribute.getDecimalRanges()[i].getUpper());
						searchRange.setMatchCount(matchCounts[i]);

						ranges.add(searchRange);
					}
					break;
				}
					
				default:
					throw new UnsupportedOperationException("Unknown attribute range type " + attributeType);
				}
				
				final SearchRangeFacetedAttributeResult rangeResult = new SearchRangeFacetedAttributeResult();
				
				rangeResult.setRanges(ranges);
				
				searchFacetedAttribute = rangeResult;
			}
			else {
				throw new UnsupportedOperationException("Unknown index faceted attribute result type " + indexFacetedAttribute.getClass());
			}

			if (indexFacetedAttribute.getNoAttributeValueCount() != 0) {
				searchFacetedAttribute.setNoAttributeValueCount(indexFacetedAttribute.getNoAttributeValueCount());
			}

			searchFacetedAttribute.setId(indexFacetedAttribute.getAttribute().getName());
			searchFacetedAttribute.setName(indexFacetedAttribute.getAttribute().getFacetDisplayName());
			
			facetAttributesResult.add(searchFacetedAttribute);
		}

		return facetAttributesResult;
	}

	private static String getTypeId(Class<? extends Item> type) {
		return ItemTypes.getTypeName(type);
	}
	
	private static String getTypeDisplayName(Class<? extends Item> type) {
		return ItemTypes.getTypeDisplayName(type);
	}
}
