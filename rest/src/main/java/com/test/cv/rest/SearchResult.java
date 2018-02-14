package com.test.cv.rest;

/**
 * Search result for one page of data
 */
public class SearchResult {

	private int totalItemMatchCount; // total number of matches, always pass along for convenience
	private int pageFirstItem; // index of item into total
	private int pageItemCount; // number of items returned, same as item array length
	
	private SearchItemResult [] items;

	public int getTotalItemMatchCount() {
		return totalItemMatchCount;
	}

	public void setTotalItemMatchCount(int totalItemMatchCount) {
		this.totalItemMatchCount = totalItemMatchCount;
	}

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

	public SearchItemResult [] getItems() {
		return items;
	}

	public void setItems(SearchItemResult[] items) {
		this.items = items;
	}
}
