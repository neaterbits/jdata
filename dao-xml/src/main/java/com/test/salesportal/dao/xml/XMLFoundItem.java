package com.test.salesportal.dao.xml;

import com.test.salesportal.dao.BaseFoundItem;
import com.test.salesportal.dao.IFoundItem;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.SortAttribute;

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
	
	@Override
	public Object getSortAttributeValue(SortAttribute attribute) {

		final Item item = getItem();
		
		return attribute.getObjectValue(item);
	}

	@Override
	public Object getFieldAttributeValue(ItemAttribute attribute) {
		return attribute.getObjectValue(getItem());
	}
}

