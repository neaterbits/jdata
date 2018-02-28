package com.test.cv.dao;

public abstract class BaseSearchItem implements SearchItem {

	private final String id;
	private final String title;
	private final Integer thumbWidth;
	private final Integer thumbHeight;
	
	protected BaseSearchItem(String id, String title, Integer thumbWidth, Integer thumbHeight) {
		this.id = id;
		this.title = title;
		this.thumbWidth = thumbWidth;
		this.thumbHeight = thumbHeight;
	}

	@Override
	public String getItemId() {
		return id;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Integer getThumbWidth() {
		return thumbWidth;
	}

	@Override
	public Integer getThumbHeight() {
		return thumbHeight;
	}
}
