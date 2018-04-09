package com.test.cv.dao.index;

import java.util.List;
import java.util.Set;

import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.SearchException;
import com.test.cv.index.IndexSearchCursor;
import com.test.cv.index.ItemIndex;
import com.test.cv.index.ItemIndexException;
import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.SortAttribute;
import com.test.cv.model.SortAttributeAndOrder;
import com.test.cv.search.SearchItem;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.facets.ItemsFacets;

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
			List<Criterium> criteria,
			List<SortAttributeAndOrder> sortOrder,
			Set<ItemAttribute> facetAttributes) throws SearchException {

		if (types == null) {
			throw new IllegalArgumentException("types == null");
		}

		IndexSearchCursor cursor;
		try {
			cursor = index.search(types, null, criteria, sortOrder, facetAttributes);
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
