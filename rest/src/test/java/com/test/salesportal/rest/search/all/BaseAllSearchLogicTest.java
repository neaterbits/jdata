package com.test.salesportal.rest.search.all;

import static org.mockito.ArgumentMatchers.eq;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.test.salesportal.dao.ISearchCursor;
import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.dao.SearchException;
import com.test.salesportal.index.IndexSearchItem;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.sports.Snowboard;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.model.criteria.SearchCriteriumValue;
import com.test.salesportal.rest.search.util.SearchSortUtil;
import com.test.salesportal.search.AttributeValues;
import com.test.salesportal.search.facets.IndexSingleValueFacet;
import com.test.salesportal.search.facets.IndexSingleValueFacetedAttributeResult;
import com.test.salesportal.search.facets.ItemsFacets;
import com.test.salesportal.search.facets.TypeFacets;

import junit.framework.TestCase;

public abstract class BaseAllSearchLogicTest extends TestCase {

	final TypeInfo snowboardType = ItemTypes.getTypeInfo(Snowboard.class);
	
	final AllSearchResult searchSnowboard(AllSearchLogic searchLogic, ISearchDAO searchDAO, Boolean getOtherSelected) {
		
		final String [] searchTypes = new String [] { snowboardType.getTypeName() };
		
		final SearchCriterium [] searchCriteria = new SearchCriterium [] {
			new SearchCriterium(
				snowboardType.getTypeName(),
				"make",
				new SearchCriteriumValue [] {
						new SearchCriteriumValue("Burton")
				},
				getOtherSelected)
		};
		
		final String [] sortOrders = new String [] {
			Snowboard.class.getSimpleName() + ':' + "make" + '_' + SearchSortUtil.ASCENDING
		};
		
		AllSearchResult result = searchLogic.search(
				searchTypes,
				null,
				searchCriteria,
				sortOrders,
				null,
				searchDAO);
		
		return result;
	}
	
	final void makeMockitoStubsForSearchResult(
			ISearchDAO searchDAO,
			ISearchCursor searchCursor,
			Snowboard snowboard,
			ItemAttribute modelVersionAttribute,
			ItemAttribute makeAttribute) throws SearchException {
		
		Mockito.when(searchDAO.search(
				ArgumentMatchers.isNotNull(),
				ArgumentMatchers.isNull(),
				ArgumentMatchers.isNotNull(),
				ArgumentMatchers.isNotNull(),
				eq(true),
				ArgumentMatchers.isNotNull(),
				ArgumentMatchers.isNotNull()))
		.thenReturn(searchCursor);
		
		Mockito.when(searchCursor.getTotalMatchCount()).thenReturn(1);

		final Map<Object, IndexSingleValueFacet> makeFacets = new HashMap<>();
		
		final IndexSingleValueFacet makeSingleValueFacet = new IndexSingleValueFacet(snowboard.getMake(), snowboard.getMake(), null);
		
		makeSingleValueFacet.increaseMatchCount();
		
		makeFacets.put(snowboard.getMake(), makeSingleValueFacet);
		
		Mockito.when(searchCursor.getFacets()).thenReturn(
				new ItemsFacets(
					Arrays.asList(
						new TypeFacets(
							Snowboard.class,
							Arrays.asList(
								new IndexSingleValueFacetedAttributeResult(
										makeAttribute,
										makeFacets))))
				));
		
		final Map<SortAttribute, Object> sortValueMap = new HashMap<>();
		sortValueMap.put(makeAttribute.makeSortAttribute(), snowboard.getMake());
		
		final Map<ItemAttribute, Object> fieldValueMap = new HashMap<>();
		fieldValueMap.put(modelVersionAttribute, snowboard.getModelVersion());
		
		Mockito.when(searchCursor.getItemIDsAndTitles(eq(0), eq(1)))
			.thenReturn(Arrays.asList(new IndexSearchItem(
				snowboard.getIdString(),
				snowboard.getTitle(),
				320,
				240,
				new AttributeValues<>(sortValueMap),
				new AttributeValues<>(fieldValueMap))));
	}
}
