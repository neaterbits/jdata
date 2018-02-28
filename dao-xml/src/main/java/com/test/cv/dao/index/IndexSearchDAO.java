package com.test.cv.dao.index;

import java.util.List;

import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.SearchException;
import com.test.cv.dao.SearchItem;
import com.test.cv.index.IndexSearchCursor;
import com.test.cv.index.ItemIndex;
import com.test.cv.index.ItemIndexException;
import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.facets.ItemsFacets;

public class IndexSearchDAO implements ISearchDAO {

	private final ItemIndex index;
	
	public IndexSearchDAO(ItemIndex index) {
		
		if (index == null) {
			throw new IllegalArgumentException("index == null");
		}

		this.index = index;
	}

	@Override
	public ISearchCursor search(List<Class<? extends Item>> types, List<Criterium> criteria, List<ItemAttribute> facetAttributes) throws SearchException {

		IndexSearchCursor cursor;
		try {
			cursor = index.search(null, criteria, null);
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
				throw new UnsupportedOperationException("TODO - should not return complete item here");
			}
			
			@Override
			public List<String> getItemIDs(int initialIdx, int count) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public ItemsFacets getFacets() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public void close() throws Exception {
		index.close();
	}
	
}
