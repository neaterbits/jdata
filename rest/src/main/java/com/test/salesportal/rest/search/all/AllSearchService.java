package com.test.salesportal.rest.search.all;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.xml.bind.Unmarshaller;

import com.test.salesportal.dao.IOperationsDAO;
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
import com.test.salesportal.rest.search.BaseSearchService;
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

@Path("/searchall")
public final class AllSearchService
	extends BaseSearchService<AllSearchItemResult, ItemSearchResult<AllSearchItemResult>> {

	private static final List<TypeInfo> ALL_TYPES = ItemTypes.getAllTypeInfosList();
	
	private static final SortedSearchResultFactory SORTED_SEARCH_RESULT_FACTORY = new SortedSearchResultFactory() {
		
		@Override
		public SortedSearchResult createSortedSearchResult(String searchResultId, List<SortAttribute> sortAttributes) {
			return new SimpleSortedSearchResult(searchResultId, sortAttributes);
		}
	};
	
	private static final SearchResultCache SEARCH_RESULT_CACHE = new SearchResultCache(SORTED_SEARCH_RESULT_FACTORY);
	
	private static final OperationDataMarshaller OPERATION_DATA_MARSHALLER = new OperationDataMarshaller();
	
	private final IOperationsDAO operationsDAO;
	
	private long currentRefereshedModelVersion;
	
	public AllSearchService(String localFileDir, IOperationsDAO operationsDAO) {
		super(localFileDir);
		
		this.operationsDAO = operationsDAO;
	}

	@Override
	protected ItemSearchResult<AllSearchItemResult> createSearchResult() {
		return new ItemSearchResult<>();
	}

	@Override
	protected AllSearchItemResult createSearchItemResult(String id, String title, Integer thumbWidth,
			Integer thumbHeight, Object[] sortFields, Object[] fields) {
		
		final long modelVersion = Long.parseLong((String)fields[0]);
		
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

	@GET
	@Path("search")
	// TODO check that we adhere to best practices for pageNo and itemsPerPage
	public AllSearchResult search(
			String [] types,
			String freeText,
			SearchCriterium [] criteria,
			String [] sortOrder,
			String [] fields,
			HttpServletRequest request) {

		// Apply any outstanding model changes to all sort results
		// Does a database query and meanwhile blocks all other searches
		
		final List<Class<? extends Item>> typesList = SearchTypesUtil.computeTypes(types);
		
		final List<SortAttributeAndOrder> sortAttributes = SearchSortUtil.decodeSortOrders(sortOrder, typesList);

		final List<ItemAttribute> responseFieldAttributes = new ArrayList<>(fields.length);

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
					request);
			
			
			final String searchResultId = SEARCH_RESULT_CACHE.cacheSearchResult(
					searchKey,
					result.getFacets(),
					result.getItems());

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

		synchronized (AllSearchService.class) {

			final List<Operation> operations = operationsDAO.getCompletedOperationsNewerThanSortedOnModelVersionAsc(currentRefereshedModelVersion);
			
			if (!operations.isEmpty()) {
				
				final Unmarshaller unmarshaller = OPERATION_DATA_MARSHALLER.createUnmarshaller();
				
				// Apply to cache in order
				for (Operation operation : operations) {

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
				
				currentRefereshedModelVersion = operations.get(operations.size() - 1).getModelVersion();
			}
		}
		
	}
}
