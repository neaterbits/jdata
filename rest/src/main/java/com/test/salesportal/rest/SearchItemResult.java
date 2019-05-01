package com.test.salesportal.rest;

/**
 * JSON serialized for one item
 */
public class SearchItemResult {

	private String id;
	private String title;
	
	// May be necessary for figuring the size of element
	private Integer thumbWidth;
	private Integer thumbHeight;
	
	// Fields as specified in the search request
	private Object [] fields;

	public SearchItemResult() {
	}

	public SearchItemResult(String id, String title, Integer thumbWidth, Integer thumbHeight, Object [] fields) {
		this.id = id;
		this.title = title;
		this.thumbWidth = thumbWidth;
		this.thumbHeight = thumbHeight;
		this.fields = fields;
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

	public Object[] getFields() {
		return fields;
	}

	public void setFields(Object[] fields) {
		this.fields = fields;
	}
}
