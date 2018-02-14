package com.test.cv.rest;

/**
 * JSON serialized for one item
 */
public class SearchItemResult {

	private String id;
	private String title;

	public SearchItemResult() {
	}

	public SearchItemResult(String id, String title) {
		super();
		this.id = id;
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
