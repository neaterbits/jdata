package com.test.salesportal.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.test.salesportal.search.SearchItem;
import com.test.salesportal.search.facets.ItemsFacets;

public class MatchNoneIndexSearchCursor implements IndexSearchCursor {

	@Override
	public List<String> getItemIDs(int initialIdx, int count) {
		return Collections.emptyList();
	}

	@Override
	public List<SearchItem> getItemIDsAndTitles(int initialIdx, int count) {
		return Collections.emptyList();
	}

	@Override
	public int getTotalMatchCount() {
		return 0;
	}

	@Override
	public ItemsFacets getFacets() {
		return new ItemsFacets(new ArrayList<>());
	}
}
