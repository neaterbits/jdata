package com.test.cv.dao.xml;

import com.test.cv.dao.BaseFoundItem;
import com.test.cv.dao.IFoundItem;
import com.test.cv.model.Item;

final class XMLFoundItem extends BaseFoundItem implements IFoundItem {

	private final String itemId;
	
	XMLFoundItem(Item item, String itemId) {
		super(item);
		
		this.itemId = itemId;
	}

	@Override
	public String getItemId() {
		return itemId;
	}
}

