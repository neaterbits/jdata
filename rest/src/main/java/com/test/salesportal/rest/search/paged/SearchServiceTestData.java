package com.test.salesportal.rest.search.paged;

import java.math.BigDecimal;
import java.util.Arrays;

import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeDecimalRangeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedTypeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;
import com.test.salesportal.rest.search.model.facetresult.SearchRangeFacetedAttributeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacet;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacetedAttributeResult;

class SearchServiceTestData {

	static PagedSearchResult makeTestResult() {
		final PagedSearchResult result = new PagedSearchResult();
		
		final SearchFacetsResult facets = new SearchFacetsResult();
		
		final SearchFacetedTypeResult sports = new SearchFacetedTypeResult();
		
		sports.setType("sports");
		sports.setDisplayName("Sports");
		
		final SearchFacetedTypeResult snowboard = new SearchFacetedTypeResult();
		
		snowboard.setType("snowboard");
		snowboard.setDisplayName("Snowboards");
		
		sports.setSubTypes(Arrays.asList(snowboard));

		final SearchSingleValueFacetedAttributeResult jonesModelAttribute = new SearchSingleValueFacetedAttributeResult();
		jonesModelAttribute.setId("model");
		jonesModelAttribute.setName("Model");
		jonesModelAttribute.setValues(Arrays.asList(
				new SearchSingleValueFacet("1234", 2),
				new SearchSingleValueFacet("5678", 1))
		);

		final SearchSingleValueFacetedAttributeResult burtonModelAttribute = new SearchSingleValueFacetedAttributeResult();
		burtonModelAttribute.setId("model");
		burtonModelAttribute.setName("Model");
		burtonModelAttribute.setValues(Arrays.asList(
				new SearchSingleValueFacet("8765", 3),
				new SearchSingleValueFacet("4321", 2))
		);

		final SearchSingleValueFacetedAttributeResult makeAttribute = new SearchSingleValueFacetedAttributeResult();
		makeAttribute.setId("make");
		makeAttribute.setName("Make");
		makeAttribute.setValues(Arrays.asList(
				new SearchSingleValueFacet("Jones", 3, jonesModelAttribute),
				new SearchSingleValueFacet("Burton", 5, burtonModelAttribute))
		);


		final SearchRangeFacetedAttributeResult lengthAttribute = new SearchRangeFacetedAttributeResult();
		
		lengthAttribute.setId("length");
		lengthAttribute.setName("Length");
		lengthAttribute.setRanges(Arrays.asList(
				new SearchFacetedAttributeDecimalRangeResult(null, new BigDecimal("160.0"), 2),
				new SearchFacetedAttributeDecimalRangeResult(new BigDecimal("160.0"), new BigDecimal("165.0"), 2),
				new SearchFacetedAttributeDecimalRangeResult(new BigDecimal("165.0"), new BigDecimal("170.0"), 3),
				new SearchFacetedAttributeDecimalRangeResult(new BigDecimal("170.0"), null, 1))
		);

		snowboard.setAttributes(Arrays.asList(makeAttribute, lengthAttribute));
		
		final SearchFacetedTypeResult housing = new SearchFacetedTypeResult();
		
		housing.setType("housing");
		housing.setDisplayName("Housing");

		final SearchFacetedTypeResult apartments = new SearchFacetedTypeResult();

		apartments.setType("apartment");
		apartments.setDisplayName("Apartments");
		
		housing.setSubTypes(Arrays.asList(apartments));
		
		facets.setTypes(Arrays.asList(sports, housing));
		
		result.setFacets(facets);
		
		return result;
	}
}
