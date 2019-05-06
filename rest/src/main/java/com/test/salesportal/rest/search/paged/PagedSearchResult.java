package com.test.salesportal.rest.search.paged;

import com.test.salesportal.rest.search.SearchItemResult;

public class PagedSearchResult extends ItemSearchResult<SearchItemResult> {

	private int pageFirstItem; // index of item into total
	private int pageItemCount; // number of items returned, same as item array length

	public int getPageFirstItem() {
		return pageFirstItem;
	}

	public void setPageFirstItem(int pageFirstItem) {
		this.pageFirstItem = pageFirstItem;
	}

	public int getPageItemCount() {
		return pageItemCount;
	}

	public void setPageItemCount(int pageItemCount) {
		this.pageItemCount = pageItemCount;
	}
}
