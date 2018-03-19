package com.test.cv.rest;

/**
 * JSON serialized for one item
 */
public class SearchItemResult {

	private String id;
	private String title;
	private Integer thumbWidth;
	private Integer thumbHeight;

	public SearchItemResult() {
	}

	public SearchItemResult(String id, String title, Integer thumbWidth, Integer thumbHeight) {
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

	public Integer getThumbWidth() {
		return thumbWidth;
	}

	public void setThumbWidth(Integer thumbWidth) {
		this.thumbWidth = thumbWidth;
	}

	public Integer getThumbHeight() {
		return thumbHeight;
	}

	public void setThumbHeight(Integer thumbHeight) {
		this.thumbHeight = thumbHeight;
	}
}
