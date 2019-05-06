package com.test.salesportal.rest.search.paged;

import com.test.salesportal.rest.search.SearchItemResult;
import com.test.salesportal.rest.search.SearchResult;

public class ItemSearchResult<T extends SearchItemResult> extends SearchResult {

	private T [] items;

	public final T [] getItems() {
		return items;
	}

	public final void setItems(T[] items) {
		this.items = items;
	}

}
