package com.test.salesportal.dao.xml;

import com.test.salesportal.dao.BaseFoundItem;
import com.test.salesportal.dao.IFoundItem;
import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttribute;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.model.items.base.TitlePhotoItem;

final class XMLFoundItem extends BaseFoundItem implements IFoundItem {

	private final String itemId;
	private final Integer thumbWidth;
	private final Integer thumbHeight;
	
	XMLFoundItem(TitlePhotoItem item, String itemId, Integer thumbWidth, Integer thumbHeight) {
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
	public Object getSortAttributeValue(SortAttribute attribute, ItemTypes itemTypes) {

		final Item item = getItem();
		
		return attribute.getObjectValue(item, itemTypes);
	}

	@Override
	public Object getFieldAttributeValue(ItemAttribute attribute) {
		return attribute.getObjectValue(getItem());
	}
}

