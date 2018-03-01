package com.test.cv.dao.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.test.cv.dao.ISearchCursor;
import com.test.cv.search.SearchItem;
import com.test.cv.search.facets.ItemsFacets;

final class JPASearchCursor implements ISearchCursor {
	private final TypedQuery<Long> countQuery;
	private final TypedQuery<Long> idQuery;
	private final Query idAndTitleQuery;

	public JPASearchCursor(TypedQuery<Long> countQuery, TypedQuery<Long> idQuery, Query itemQuery) {
		this.countQuery = countQuery;
		this.idQuery = idQuery;
		this.idAndTitleQuery = itemQuery;
	}

	@Override
	public List<String> getItemIDs(int initialIdx, int count) {

		idQuery.setFirstResult(initialIdx);
		idQuery.setMaxResults(count);

		return idQuery.getResultList().stream()
				.map(id -> String.valueOf(id))
				.collect(Collectors.toList());
	}

	@Override
	public List<SearchItem> getItemIDsAndTitles(int initialIdx, int count) {
		idAndTitleQuery.setFirstResult(initialIdx);
		idAndTitleQuery.setMaxResults(count);
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<Object[]> items = (List)idAndTitleQuery.getResultList();
		
		final List<SearchItem> result = new ArrayList<>(items.size());
		
		for (Object [] row : items) {
			final SearchItem searchItem = JPASearchItem.mapFromRow(row);
			
			result.add(searchItem);
		}

		return result;
	}

	@Override
	public int getTotalMatchCount() {
		return countQuery.getSingleResult().intValue();
	}

	@Override
	public ItemsFacets getFacets() {
		throw new UnsupportedOperationException("No facet attributes specified in search query so none in result");
	}
}
