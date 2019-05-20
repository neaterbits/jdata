package com.test.salesportal.dao.index;

import java.util.List;
import java.util.Set;

import com.test.salesportal.dao.ISearchCursor;
import com.test.salesportal.dao.ISearchDAO;
import com.test.salesportal.dao.SearchException;
import com.test.salesportal.index.IndexSearchCursor;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.index.ItemIndexException;
import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttributeAndOrder;
import com.test.salesportal.search.SearchItem;
import com.test.salesportal.search.criteria.Criterium;
import com.test.salesportal.search.facets.ItemsFacets;

public class IndexSearchDAO implements ISearchDAO {

	private final ItemIndex index;
	private final boolean closeIndexOnClose;
	
	
	public IndexSearchDAO(ItemIndex index, boolean closeIndexOnClose) {
		
		if (index == null) {
			throw new IllegalArgumentException("index == null");
		}

		this.index = index;
		this.closeIndexOnClose = closeIndexOnClose;
	}

	@Override
	public ISearchCursor search(
			List<Class<? extends Item>> types,
			String freeText,
			List<Criterium> criteria,
			List<SortAttributeAndOrder> sortOrder,
			boolean returnSortAttributeValues,
			Set<ItemAttribute> fieldAttributes,
			Set<ItemAttribute> facetAttributes) throws SearchException {

		if (types == null) {
			throw new IllegalArgumentException("types == null");
		}

		IndexSearchCursor cursor;
		try {
			cursor = index.search(types, freeText, criteria, sortOrder, returnSortAttributeValues, fieldAttributes, facetAttributes);
		} catch (ItemIndexException ex) {
			throw new SearchException("Failed to search", ex);
		}
		
		return new ISearchCursor() {
			
			@Override
			public int getTotalMatchCount() {
				return cursor.getTotalMatchCount();
			}
			
			@Override
			public List<SearchItem> getItemIDsAndTitles(int initialIdx, int count) {
				return cursor.getItemIDsAndTitles(initialIdx, count);
			}
			
			@Override
			public List<String> getItemIDs(int initialIdx, int count) {
				return cursor.getItemIDs(initialIdx, count);
			}
			
			@Override
			public ItemsFacets getFacets() {
				return cursor.getFacets();
			}
		};
	}

	@Override
	public void close() throws Exception {
		if (closeIndexOnClose) {
			index.close();
		}
	}
	
}
