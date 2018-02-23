package com.test.cv.dao.xml;

import com.test.cv.dao.BaseFoundItem;
import com.test.cv.dao.IFoundItem;
import com.test.cv.model.Item;

final class XMLFoundItem extends BaseFoundItem implements IFoundItem {

	private final String itemId;
	private final Integer thumbWidth;
	private final Integer thumbHeight;
	
	XMLFoundItem(Item item, String itemId, Integer thumbWidth, Integer thumbHeight) {
		super(item);
		
		this.itemId = itemId;
		this.thumbWidth = thumbWidth;
		this.thumbHeight = thumbHeight;
	}

	@Override
	public String getItemId() {
		return itemId;
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

