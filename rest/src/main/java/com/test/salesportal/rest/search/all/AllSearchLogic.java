package com.test.salesportal.rest.search.all;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.Unmarshaller;

import com.test.salesportal.dao.IOperationsRetrieval;
import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.model.SortAttributeAndOrder;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.operations.Operation;
import com.test.salesportal.model.operations.dao.BaseOperationData;
import com.test.salesportal.model.operations.dao.DeleteItemOperationData;
import com.test.salesportal.model.operations.dao.OperationDataMarshaller;
import com.test.salesportal.model.operations.dao.StoreItemOperationData;
import com.test.salesportal.model.operations.dao.UpdateItemOperationData;
import com.test.salesportal.rest.search.BaseSearchLogic;
import com.test.salesportal.rest.search.all.cache.CachedSearchResult;
import com.test.salesportal.rest.search.all.cache.SearchKey;
import com.test.salesportal.rest.search.all.cache.SearchResultCache;
import com.test.salesportal.rest.search.all.cache.SimpleSortedSearchResult;
import com.test.salesportal.rest.search.all.cache.SortedSearchResult;
import com.test.salesportal.rest.search.all.cache.SortedSearchResultFactory;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.model.sorting.SearchSortOrderAlternative;
import com.test.salesportal.rest.search.paged.ItemSearchResult;
import com.test.salesportal.rest.search.util.SearchSortUtil;
import com.test.salesportal.rest.search.util.SearchTypesUtil;

final class AllSearchLogic extends BaseSearchLogic<AllSearchItemResult, ItemSearchResult<AllSearchItemResult>> {

	private static final List<TypeInfo> ALL_TYPES = ItemTypes.getAllTypeInfosList();
	
	private static final SortedSearchResultFactory SORTED_SEARCH_RESULT_FACTORY = new SortedSearchResultFactory() {
		
		@Override
		public SortedSearchResult createSortedSearchResult(String searchResultId, List<SortAttribute> sortAttributes) {
			return new SimpleSortedSearchResult(searchResultId, sortAttributes);
		}
	};
	
	private static final SearchResultCache SEARCH_RESULT_CACHE = new SearchResultCache(SORTED_SEARCH_RESULT_FACTORY);
	
	private static final OperationDataMarshaller OPERATION_DATA_MARSHALLER = new OperationDataMarshaller();
	
	private final IOperationsRetrieval operationsRetrieval;
	private final ITestCriticalSectionCallback criticalSectionCallback;
	
	private long currentRefreshedModelVersion;

	AllSearchLogic(IOperationsRetrieval operationsRetrieval, ITestCriticalSectionCallback criticalSectionCallback) {

		if (operationsRetrieval == null) {
			throw new IllegalArgumentException("operationsRetrieval == null");
		}
		
		this.operationsRetrieval = operationsRetrieval;
		this.criticalSectionCallback = criticalSectionCallback;
		this.currentRefreshedModelVersion = -1L;
	}

	@Override
	protected ItemSearchResult<AllSearchItemResult> createSearchResult() {
		return new ItemSearchResult<>();
	}

	@Override
	protected AllSearchItemResult createSearchItemResult(String id, String title, Integer thumbWidth,
			Integer thumbHeight, Object[] sortFields, Object[] fields) {
		
		final long modelVersion = (Long)fields[0];
		
		final Object [] fieldsWithoutModelVersion = Arrays.copyOfRange(fields, 1, fields.length);
		
		return new AllSearchItemResult(
				modelVersion,
				id,
				title,
				thumbWidth, thumbHeight,
				sortFields,
				fieldsWithoutModelVersion);
	}

	@Override
	protected AllSearchItemResult[] createSearchItemArray(int length) {
		return new AllSearchItemResult[length];
	}

	@Override
	protected void setPageFirstItem(ItemSearchResult<AllSearchItemResult> result, int firstItem) {
		
	}

	@Override
	protected void setPageItemCount(ItemSearchResult<AllSearchItemResult> result, int count) {
		
	}

	// TODO check that we adhere to best practices for pageNo and itemsPerPage
	public AllSearchResult search(
			String [] types,
			String freeText,
			SearchCriterium [] criteria,
			String [] sortOrder,
			String [] fields,
			ISearchDAO searchDAO) {

		// Apply any outstanding model changes to all sort results
		// Does a database query and meanwhile blocks all other searches
		
		final List<Class<? extends Item>> typesList = SearchTypesUtil.computeTypes(types);
		
		final List<SortAttributeAndOrder> sortAttributes = SearchSortUtil.decodeSortOrders(sortOrder, typesList);

		final int fieldsLength = (fields != null ? fields.length : 0) + 1;
		final List<ItemAttribute> responseFieldAttributes = new ArrayList<>(fieldsLength);

		for (Class<? extends Item> type : typesList) {

			final TypeInfo typeInfo = ItemTypes.getTypeInfo(type);
			final ItemAttribute attribute = typeInfo.getAttributes().getByName(Item.MODEL_VERSION);

			if (attribute == null) {
				throw new IllegalStateException();
			}
			
			responseFieldAttributes.add(attribute);
		}

		
		final SearchKey searchKey = new SearchKey(
				typesList,
				freeText,
				criteria,
				sortAttributes.stream()
					.map(SortAttributeAndOrder::getAttribute)
					.collect(Collectors.toList()),
				responseFieldAttributes);
		
		
		CachedSearchResult cachedSearchResult = SEARCH_RESULT_CACHE.getCachedSearchResult(searchKey);
		
		final AllSearchResult allSearchResult;
		
		final SearchSortOrderAlternative [] possibleSortOrders = SearchSortUtil.computeAndSortPossibleSortOrders(typesList);
		
		if (cachedSearchResult != null) {
			
			// This will scan update log in database
			refreshCacheIfOutdated();

			cachedSearchResult = SEARCH_RESULT_CACHE.getCachedSearchResult(searchKey);

			if (cachedSearchResult == null) {
				throw new IllegalStateException();
			}
			
			allSearchResult = new AllSearchResult(
					cachedSearchResult.getTotalItemMatchCount(),
					possibleSortOrders,
					cachedSearchResult.getFacets(),
					cachedSearchResult.getSearchResultId());
			
		}
		else {
			// Must retrieve fields for sort attributes so that can cache values

			if (fields != null && fields.length > 0) {
	
				for (Class<? extends Item> type : typesList) {

					final TypeInfo typeInfo = ItemTypes.getTypeInfo(type);

					for (String field : fields) {
						
						final ItemAttribute attribute = typeInfo.getAttributes().getByName(field);
						
						if (attribute != null) {
							responseFieldAttributes.add(attribute);
							break;
						}
					}
				}
			}

			final ItemSearchResult<AllSearchItemResult> result = searchInDB(
					typesList,
					freeText,
					criteria,
					sortAttributes,
					true,
					responseFieldAttributes,
					null,
					null,
					searchDAO);

			final String searchResultId;
			
			synchronized (AllSearchLogic.class) {
			
				if (criticalSectionCallback != null) {
					criticalSectionCallback.invoke();
				}

				searchResultId = SEARCH_RESULT_CACHE.cacheSearchResult(
					searchKey,
					result.getFacets(),
					result.getItems());
			}

			allSearchResult = new AllSearchResult(
					result.getTotalItemMatchCount(),
					possibleSortOrders,
					result.getFacets(),
					searchResultId);
		}

		return allSearchResult;
	}

	/**
	 * Refreshes all cache items by reading by applying all model operations.
	 * 
	 * Done with synchronization on so that will have other threads wait, even while
	 * querying the database.
	 * 
	 * Blocking other threads does not matter,
	 * since alternatively those threads would also go to DB.
	 * 
	 * 
	 */
	
	private void refreshCacheIfOutdated() {

		final List<Operation> operations = operationsRetrieval.getCompletedOperationsNewerThanSortedOnModelVersionAsc(currentRefreshedModelVersion);

		synchronized (AllSearchService.class) {

			if (criticalSectionCallback != null) {
				criticalSectionCallback.invoke();
			}
			
			if (!operations.isEmpty()) {
				
				final Unmarshaller unmarshaller = OPERATION_DATA_MARSHALLER.createUnmarshaller();
				
				// Apply to cache in order
				for (Operation operation : operations) {
					
					if (currentRefreshedModelVersion == -1) {
						this.currentRefreshedModelVersion = operation.getModelVersion();
					}
					else {
						if (currentRefreshedModelVersion != operation.getModelVersion() - 1) {
							throw new IllegalStateException("Non contiguous model versions");
						}
						else {
							++ this.currentRefreshedModelVersion;
						}
					}

					final BaseOperationData operationData = OPERATION_DATA_MARSHALLER.decodeOperationData(unmarshaller, operation);

					if (operationData instanceof StoreItemOperationData) {
						
						final StoreItemOperationData storeItemOperationData = (StoreItemOperationData)operationData;
						
						SEARCH_RESULT_CACHE.applyToAnyMatchingCachedSearchResults(storeItemOperationData.getItem(), ALL_TYPES);
						
					}
					else if (operationData instanceof UpdateItemOperationData) {

						final UpdateItemOperationData updateItemOperationData = (UpdateItemOperationData)operationData;
						
						SEARCH_RESULT_CACHE.applyToAnyMatchingCachedSearchResults(updateItemOperationData.getItem(), ALL_TYPES);
						
					}
					else if (operationData instanceof DeleteItemOperationData) {
						
						final DeleteItemOperationData deleteItemOperationData = (DeleteItemOperationData)operationData;
						
						SEARCH_RESULT_CACHE.deleteFromCachedSearchResults(deleteItemOperationData.getItemId());
					}
					else {
						throw new UnsupportedOperationException("Unknown operation type " + operationData.getClass().getName());
					}
				}
				
				currentRefreshedModelVersion = operations.get(operations.size() - 1).getModelVersion();
			}
		}
		
	}
}
