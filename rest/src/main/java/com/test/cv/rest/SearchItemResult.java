package com.test.cv.rest;

/**
 * JSON serialized for one item
 */
public class SearchItemResult {

	private String id;
	private String title;
	private int thumbWidth;
	private int thumbHeight;

	public SearchItemResult() {
	}

	public SearchItemResult(String id, String title, int thumbWidth, int thumbHeight) {
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

	public int getThumbWidth() {
		return thumbWidth;
	}

	public void setThumbWidth(int thumbWidth) {
		this.thumbWidth = thumbWidth;
	}

	public int getThumbHeight() {
		return thumbHeight;
	}

	public void setThumbHeight(int thumbHeight) {
		this.thumbHeight = thumbHeight;
	}
}
