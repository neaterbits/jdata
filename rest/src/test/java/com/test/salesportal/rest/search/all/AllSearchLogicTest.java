package com.test.salesportal.rest.search.all;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mockito.Mockito;
import static org.mockito.Mockito.times;

import com.test.salesportal.common.CollectionUtil;
import com.test.salesportal.common.UUIDGenerator;
import com.test.salesportal.dao.IOperationsDAO;
import com.test.salesportal.dao.ISearchCursor;
import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.SortAttributeAndOrder;
import com.test.salesportal.model.SortOrder;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.sports.Snowboard;
import com.test.salesportal.model.operations.Operation;
import com.test.salesportal.model.operations.dao.OperationDataMarshaller;
import com.test.salesportal.model.operations.dao.StoreItemOperationData;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedTypeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacet;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacetedAttributeResult;
import com.test.salesportal.search.criteria.InCriteriumValue;
import com.test.salesportal.search.criteria.StringInCriterium;

import static com.test.salesportal.rest.search.all.AllSearchTestUtil.findAttribute;

import org.mockito.ArgumentMatchers;

import static org.mockito.ArgumentMatchers.eq;

import static org.assertj.core.api.Assertions.assertThat;

public class AllSearchLogicTest extends BaseAllSearchLogicTest {

	public void testSearchWithUpdateFromOperationsLog() throws Exception {

		final IOperationsDAO operationsDAO = Mockito.mock(IOperationsDAO.class);
		final ISearchDAO searchDAO = Mockito.mock(ISearchDAO.class);
		
		final AllSearchLogic searchLogic = new AllSearchLogic(operationsDAO, null);
		
		final ItemAttribute modelVersionAttribute = snowboardType.getAttributes().getByName("modelVersion");
		final ItemAttribute makeAttribute = snowboardType.getAttributes().getByName("make");
		
		final ISearchCursor searchCursor = Mockito.mock(ISearchCursor.class);

		final Snowboard snowboard = new Snowboard();

		snowboard.setModelVersion(0L);
		snowboard.setIdString(UUIDGenerator.generateUUID());
		snowboard.setTitle("Snowboard title");
		snowboard.setMake("Burton");
		snowboard.setProductionYear(2015);
		
		makeMockitoStubsForSearchResult(searchDAO, searchCursor, snowboard, modelVersionAttribute, makeAttribute);
		
		final boolean getOtherSelected = false;

		AllSearchResult result = searchSnowboard(searchLogic, searchDAO, getOtherSelected);
		
		final Set<ItemAttribute> facetAttributes = snowboardType.getAttributes().asSet().stream()
				.filter(ItemAttribute::isFaceted)
				.collect(Collectors.toSet());
		
		Mockito.verify(searchDAO, times(1)).search(
				eq(Arrays.asList(Snowboard.class)),
				ArgumentMatchers.isNull(),
				eq(Arrays.asList(new StringInCriterium(
						makeAttribute,
						Arrays.asList(new InCriteriumValue<String>("Burton", null)),
						getOtherSelected))),
				eq(Arrays.asList(new SortAttributeAndOrder(makeAttribute.makeSortAttribute(), SortOrder.ASCENDING))),
				eq(true),
				eq(CollectionUtil.asSet(modelVersionAttribute)),
				eq(facetAttributes));
		
		Mockito.verify(searchCursor, times(1)).getTotalMatchCount();
		Mockito.verify(searchCursor, times(1)).getItemIDsAndTitles(eq(0), eq(1));
		Mockito.verify(searchCursor, times(1)).getFacets();
		
		Mockito.verify(searchDAO, times(1)).close();
		
		Mockito.verifyNoMoreInteractions(searchDAO, searchCursor, operationsDAO);

		checkResult(result, snowboard, snowboardType, makeAttribute);
		
		Mockito.when(operationsDAO.getCompletedOperationsNewerThanSortedOnModelVersionAsc(ArgumentMatchers.anyLong()))
			.thenReturn(Collections.emptyList());
		
		// Read once more, from cache
		result = searchSnowboard(searchLogic, searchDAO, getOtherSelected);

		Mockito.verify(operationsDAO, times(1)).getCompletedOperationsNewerThanSortedOnModelVersionAsc(eq(-1L));
		
		Mockito.verifyNoMoreInteractions(searchDAO, searchCursor, operationsDAO);
		
		Mockito.reset(operationsDAO);
		
		checkResult(result, snowboard, snowboardType, makeAttribute);
		
		
		final long anotherModelVersion = 1L;
		
		final Snowboard anotherSnowboard = new Snowboard();
		
		anotherSnowboard.setIdString(UUIDGenerator.generateUUID());
		anotherSnowboard.setModelVersion(anotherModelVersion);
		anotherSnowboard.setMake("Burton");
		anotherSnowboard.setModel("Some model");
		
		// Check once more with update from operations log
		final OperationDataMarshaller operationDataMarshaller = new OperationDataMarshaller();
		
		final String userId = "theUser";
		
		final byte [] storeData = operationDataMarshaller.encodeOperationData(new StoreItemOperationData(userId, anotherSnowboard));
		
		final Operation operation = new Operation(new Date(), storeData, userId);
		
		// Normally set by relational DB
		operation.setId(anotherModelVersion);
		
		Mockito.when(operationsDAO.getCompletedOperationsNewerThanSortedOnModelVersionAsc(ArgumentMatchers.anyLong()))
			.thenReturn(Arrays.asList(operation));
	
		// Read once more, from cache
		final AllSearchResult anotherResult = searchSnowboard(
				searchLogic,
				searchDAO,
				getOtherSelected);

		Mockito.verify(operationsDAO).getCompletedOperationsNewerThanSortedOnModelVersionAsc(eq(-1L));

		Mockito.verifyNoMoreInteractions(searchDAO, searchCursor, operationsDAO);

		Mockito.reset(operationsDAO);

		assertThat(anotherResult).isNotNull();
		checkResultId(anotherResult);
		assertThat(anotherResult.getSearchResultId()).isEqualTo(result.getSearchResultId());
		assertThat(anotherResult.getTotalItemMatchCount()).isEqualTo(2);
		
		assertThat(anotherResult.getFacets().getTypes().size()).isEqualTo(ItemTypes.getAllTypesSet().size());
		
		final SearchFacetedTypeResult typeResult = CollectionUtil.find(
				anotherResult.getFacets().getTypes(),
				type -> type.getType().equals(snowboardType.getTypeName()));
		
		assertThat(typeResult).isNotNull();
		assertThat(typeResult.getAttributes().size()).isEqualTo(6);
		
		final SearchSingleValueFacetedAttributeResult makeAttributeResult
			= (SearchSingleValueFacetedAttributeResult)findAttribute(typeResult.getAttributes(), "make");
		
		assertThat(makeAttributeResult.getId()).isEqualTo("make");
		assertThat(makeAttributeResult.getName()).isEqualTo("Make");
		assertThat(makeAttributeResult.getNoAttributeValueCount()).isNull();
		assertThat(makeAttributeResult.getValues().size()).isEqualTo(1);
		assertThat(makeAttributeResult.getValues().get(0).getValue()).isEqualTo("Burton");
		assertThat(makeAttributeResult.getValues().get(0).getMatchCount()).isEqualTo(2);
		assertThat(makeAttributeResult.getValues().get(0).getSubAttributes().size()).isEqualTo(1);
		
		// Check that operations modelversion has been updated
		Mockito.when(operationsDAO.getCompletedOperationsNewerThanSortedOnModelVersionAsc(ArgumentMatchers.anyLong()))
			.thenReturn(Collections.emptyList());

		// Read once more, from cache
		searchSnowboard(searchLogic, searchDAO, getOtherSelected);

		Mockito.verify(operationsDAO).getCompletedOperationsNewerThanSortedOnModelVersionAsc(eq(1L));
	
		Mockito.verifyNoMoreInteractions(searchDAO, searchCursor, operationsDAO);
	}
	
	private void checkResult(AllSearchResult result, Snowboard snowboard, TypeInfo snowboardType, ItemAttribute makeAttribute) {
		assertThat(result).isNotNull();
		
		checkResultId(result);
		
		checkSortOrders(result);

		assertThat(result.getTotalItemMatchCount()).isEqualTo(1);
		
		assertThat(result.getFacets()).isNotNull();
		assertThat(result.getFacets().getTypes().size()).isEqualTo(1);
		assertThat(result.getFacets().getTypes().get(0).getType()).isEqualTo(snowboardType.getTypeName());
		assertThat(result.getFacets().getTypes().get(0).getDisplayName()).isEqualTo(snowboardType.getFacetDisplayName());
		assertThat(result.getFacets().getTypes().get(0).getAttributes().size()).isEqualTo(1);
		
		final SearchSingleValueFacetedAttributeResult makeFacet = (SearchSingleValueFacetedAttributeResult)result.getFacets().getTypes().get(0).getAttributes().get(0);
	
		checkMakeFacet(makeFacet, snowboard, makeAttribute);
	}
	
	private void checkResultId(AllSearchResult result) {
		
		assertThat(result.getSearchResultId()).isNotNull();
		assertThat(UUID.fromString(result.getSearchResultId())).isNotNull();
	}
	
	private void checkSortOrders(AllSearchResult result) {
		assertThat(result.getSortOrders().length).isEqualTo(5);
		assertThat(result.getSortOrders()[0].getName()).isEqualTo("Item:title");
		assertThat(result.getSortOrders()[0].getDisplayName()).isEqualTo("Title");
		assertThat(result.getSortOrders()[1].getName()).isEqualTo("RetailItem:make");
		assertThat(result.getSortOrders()[1].getDisplayName()).isEqualTo("Make");
		assertThat(result.getSortOrders()[2].getName()).isEqualTo("RetailItem:model");
		assertThat(result.getSortOrders()[2].getDisplayName()).isEqualTo("Model");
		assertThat(result.getSortOrders()[3].getName()).isEqualTo("RetailItem:productionYear_ascending");
		assertThat(result.getSortOrders()[3].getDisplayName()).isEqualTo("Production year - low to high");
		assertThat(result.getSortOrders()[4].getName()).isEqualTo("RetailItem:productionYear_descending");
		assertThat(result.getSortOrders()[4].getDisplayName()).isEqualTo("Production year - high to low");
	}
	
	private void checkMakeFacet(SearchSingleValueFacetedAttributeResult makeFacet, Snowboard snowboard, ItemAttribute makeAttribute) {
		assertThat(makeFacet.getId()).isEqualTo(makeAttribute.getName());
		assertThat(makeFacet.getName()).isEqualTo(makeAttribute.getFacetDisplayName());
		assertThat(makeFacet.getNoAttributeValueCount()).isNull();
		assertThat(makeFacet.getValues().size()).isEqualTo(1);
		
		checkMakeFacetValue(makeFacet.getValues().get(0), snowboard);
	}
	
	private void checkMakeFacetValue(SearchSingleValueFacet valueFacet, Snowboard snowboard) {
		assertThat(valueFacet.getValue()).isEqualTo(snowboard.getMake());
		assertThat(valueFacet.getMatchCount()).isEqualTo(1);
		assertThat(valueFacet.getSubAttributes()).isNull();
	}
}
