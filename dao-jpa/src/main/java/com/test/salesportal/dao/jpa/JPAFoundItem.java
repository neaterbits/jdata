package com.test.salesportal.dao.jpa;


import com.test.salesportal.dao.BaseFoundItem;
import com.test.salesportal.dao.IFoundItem;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttribute;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.model.items.base.TitlePhotoItem;

final class JPAFoundItem extends BaseFoundItem implements IFoundItem {

	JPAFoundItem(TitlePhotoItem item) {
		super(item);
	}

	@Override
	public String getItemId() {
		return String.valueOf(getItem().getId());
	}

	@Override
	public Integer getThumbWidth() {
		return getItem().getThumbWidth();
	}

	@Override
	public Integer getThumbHeight() {
		return getItem().getThumbHeight();
	}
	
	@Override
	public Object getSortAttributeValue(SortAttribute attribute, ItemTypes itemTypes) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Object getFieldAttributeValue(ItemAttribute attribute) {
		throw new UnsupportedOperationException("TODO");
	}
}
