package com.test.salesportal.dao.jpa;

import java.util.List;
import java.util.stream.Collectors;

import com.test.salesportal.dao.ISearchCursor;
import com.test.salesportal.search.SearchItem;
import com.test.salesportal.search.facets.ItemsFacets;

/**
 * When returning facets from DB, we must have retrieved all matching items
 * from DB since there is no standard way to retrieve facets in JPQL.
 * group-by does work for this since cannot group independently among columns.
 * Thus must just retrieve all matching items and count them, like for Lucene index
 */

final class JPASearchCursorWithFacets implements ISearchCursor {

	private final List<JPASearchItem> matches;
	private final ItemsFacets facets;
	
	JPASearchCursorWithFacets(List<JPASearchItem> matches, ItemsFacets facets) {
		if (matches == null) {
			throw new IllegalArgumentException("matches == null");
		}
		
		if (facets == null) {
			throw new IllegalArgumentException("facets == null");
		}
		
		this.matches = matches;
		this.facets = facets;
	}

	@Override
	public List<String> getItemIDs(int initialIdx, int count) {
		return matches.stream()
				.skip(initialIdx)
				.limit(count)
				.map(item -> item.getItemId())
				.collect(Collectors.toList());
	}

	@Override
	public List<SearchItem> getItemIDsAndTitles(int initialIdx, int count) {
		return matches.stream()
				.skip(initialIdx)
				.limit(count)
				.collect(Collectors.toList());
	}

	@Override
	public int getTotalMatchCount() {
		return matches.size();
	}

	@Override
	public ItemsFacets getFacets() {
		return facets;
	}
}
