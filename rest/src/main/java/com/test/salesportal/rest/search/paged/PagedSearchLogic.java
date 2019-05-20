package com.test.salesportal.rest.search.paged;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttributeAndOrder;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.rest.search.BaseSearchLogic;
import com.test.salesportal.rest.search.SearchItemResult;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.util.SearchSortUtil;
import com.test.salesportal.rest.search.util.SearchTypesUtil;

final class PagedSearchLogic extends BaseSearchLogic<SearchItemResult, PagedSearchResult> {

	
	PagedSearchLogic(ItemTypes itemTypes) {
		super(itemTypes);
	}

	@Override
	protected PagedSearchResult createSearchResult() {
		return new PagedSearchResult();
	}

	@Override
	protected SearchItemResult createSearchItemResult(String id, String title, Integer thumbWidth, Integer thumbHeight,
			Object[] sortFields, Object[] fields) {
		return new SearchItemResult(id, title, thumbWidth, thumbHeight, sortFields, fields);
	}

	@Override
	protected SearchItemResult[] createSearchItemArray(int length) {
		return new SearchItemResult[length];
	}

	@Override
	protected void setPageFirstItem(PagedSearchResult result, int firstItem) {
		result.setPageFirstItem(firstItem);
	}

	@Override
	protected void setPageItemCount(PagedSearchResult result, int count) {
		result.setPageItemCount(count);
	}
	
	public PagedSearchResult search(
			String [] types,
			String freeText,
			SearchCriterium [] criteria,
			String [] sortOrder,
			String [] fields,
			Integer pageNo,
			Integer itemsPerPage,
			Boolean testdata,
			ISearchDAO searchDAO) {
		
		final ItemTypes itemTypes = getItemTypes();

		final List<Class<? extends Item>> typesList = SearchTypesUtil.computeTypes(types, itemTypes);
		
		final List<SortAttributeAndOrder> sortAttributes = SearchSortUtil.decodeSortOrders(sortOrder, typesList, itemTypes);
		
		final List<ItemAttribute> responseFieldAttributes;
		if (fields != null && fields.length > 0) {
			responseFieldAttributes = new ArrayList<>();

			for (String field : fields) {
				for (Class<? extends Item> type : typesList) {
					final TypeInfo typeInfo = itemTypes.getTypeInfo(type);
					
					final ItemAttribute attribute = typeInfo.getAttributes().getByName(field);
					
					if (attribute != null) {
						responseFieldAttributes.add(attribute);
						break;
					}
				}
			}
		}
		else {
			responseFieldAttributes = Collections.emptyList();
		}

		final PagedSearchResult result;
		if (testdata != null && testdata && isTest()) {
			// Return a hardcoded testresult for simple local testing
			result = SearchServiceTestData.makeTestResult();
		}
		else {
			result = searchInDB(
					typesList,
					freeText,
					criteria,
					sortAttributes,
					false,
					responseFieldAttributes,
					pageNo,
					itemsPerPage,
					searchDAO);
		}

		return result;
	}
}
