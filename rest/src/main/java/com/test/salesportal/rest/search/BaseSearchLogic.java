package com.test.salesportal.rest.search;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.test.salesportal.dao.ISearchCursor;
import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.dao.SearchException;
import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttributeAndOrder;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.rest.BaseServiceLogic;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.paged.ItemSearchResult;
import com.test.salesportal.rest.search.util.SearchCriteriaUtil;
import com.test.salesportal.rest.search.util.SearchFacetsUtil;
import com.test.salesportal.rest.search.util.SearchSortUtil;
import com.test.salesportal.search.SearchItem;
import com.test.salesportal.search.criteria.Criterium;
import com.test.salesportal.search.facets.ItemsFacets;

public abstract class BaseSearchLogic<
		ITEM extends SearchItemResult,
		RESULT extends ItemSearchResult<ITEM>> extends BaseServiceLogic {

	protected abstract RESULT createSearchResult();
	protected abstract ITEM createSearchItemResult(
			String id,
			String title,
			Integer thumbWidth,
			Integer thumbHeight,
			Object [] sortFields,
			Object [] fields);
	
	protected abstract ITEM [] createSearchItemArray(int length);
	
	protected abstract void setPageFirstItem(RESULT result, int firstItem);
	protected abstract void setPageItemCount(RESULT result, int count);
	
	private final ItemTypes itemTypes;
	
	protected BaseSearchLogic(ItemTypes itemTypes) {

		if (itemTypes == null) {
			throw new IllegalArgumentException("itemTypes == null");
		}
		
		this.itemTypes = itemTypes;
	}
	
	protected final RESULT searchInDB(
			List<Class<? extends Item>> types,
			String freeText,
			SearchCriterium [] criteria,
			List<SortAttributeAndOrder> sortAttributes,
			boolean returnSortAttributeValues,
			List<ItemAttribute> responseFieldAttributes,
			Integer pageNo, Integer itemsPerPage,
			ISearchDAO searchDAO) {
		

		final List<Criterium> daoCriteria; 
		if (criteria != null) {
			daoCriteria = SearchCriteriaUtil.convertCriteria(criteria, itemTypes);
		}
		else {
			daoCriteria = null;
		}

		// TODO support types
		final RESULT result = createSearchResult();
			
		try {
			final ISearchCursor cursor;
			try {
				final Set<ItemAttribute> responseFieldSet =
						(responseFieldAttributes != null && !responseFieldAttributes.isEmpty())
						? new HashSet<>(responseFieldAttributes)
						: Collections.emptySet();

				cursor = searchDAO.search(
						types,
						freeText,
						daoCriteria,
						sortAttributes,
						returnSortAttributeValues,
						responseFieldSet,
						itemTypes.getFacetAttributes(types));
				
			} catch (SearchException ex) {
				throw new IllegalStateException("Failed to search", ex);
			}
	
			final int totalMatchCount = cursor.getTotalMatchCount();
			
			final int initialIdx;
			final int count;
			if (pageNo != null && itemsPerPage != null) {
				if (pageNo < 1) {
					throw new IllegalArgumentException("pageNo < 1");
				}
				
				initialIdx = itemsPerPage * (pageNo - 1); // starts at 1
				count = itemsPerPage;
			}
			else {
				// return all results
				initialIdx = 0;
				count = totalMatchCount;
			}
			
			final List<SearchItem> found = cursor.getItemIDsAndTitles(initialIdx, count);
			
			final int numFound = found.size();
			
			setPageFirstItem(result, initialIdx);
			setPageItemCount(result, numFound);
			result.setTotalItemMatchCount(totalMatchCount);
			
			final ITEM [] items = createSearchItemArray(numFound);

			final Set<Class<? extends Item>> sortOrderTypes = new HashSet<>(types);

			final ItemsFacets facets = cursor.getFacets();
			
			if (facets != null) {
				
				// If facets, only add types from facet result
				final List<Class<? extends Item>> facetTypes = facets.getTypes()
						.stream()
						.filter(facetType -> !facetType.getAttributes().isEmpty())
						.map(facetType -> facetType.getType())
						.collect(Collectors.toList());
				
				sortOrderTypes.retainAll(facetTypes);
				
				result.setFacets(SearchFacetsUtil.convertFacets(facets, itemTypes));
			}
			
			result.setSortOrders(SearchSortUtil.computeAndSortPossibleSortOrders(sortOrderTypes, itemTypes));

			for (int i = 0; i < numFound; ++ i) {
				final SearchItem foundItem = found.get(i);
				
				final Object [] sortValues;
				
				if (returnSortAttributeValues && sortAttributes != null && !sortAttributes.isEmpty()) {
					sortValues = new Object[sortAttributes.size()];
				
					for (int sortAttributeNo = 0; sortAttributeNo < sortAttributes.size(); ++ sortAttributeNo) {
						sortValues[sortAttributeNo] = foundItem.getSortAttributeValue(sortAttributes.get(sortAttributeNo).getAttribute(), itemTypes);
					}
				}
				else {
					sortValues = null;
				}
				
				final Object [] fieldValues = new Object[responseFieldAttributes.size()];

				for (int fieldNo = 0; fieldNo < responseFieldAttributes.size(); ++ fieldNo) {
					final ItemAttribute attribute = responseFieldAttributes.get(fieldNo);

					fieldValues[fieldNo] = foundItem.getFieldAttributeValue(attribute);
				}

				items[i] = createSearchItemResult(
						foundItem.getItemId(),
						foundItem.getTitle(),
						foundItem.getThumbWidth() != null ? foundItem.getThumbWidth() : THUMBNAIL_MAX_SIZE,
						foundItem.getThumbHeight() != null ? foundItem.getThumbHeight() : THUMBNAIL_MAX_SIZE,
						sortValues,
						fieldValues);
			}
			
			result.setItems(items);
			
		}
		finally {
			try {
				searchDAO.close();
			} catch (Exception ex) {
				throw new IllegalStateException("Failed to close search DAO", ex);
			}
		}

		return result;
	}

	protected final ItemTypes getItemTypes() {
		return itemTypes;
	}
}
