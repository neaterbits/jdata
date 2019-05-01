package com.test.salesportal.dao.jpa;


import com.test.salesportal.dao.BaseFoundItem;
import com.test.salesportal.dao.IFoundItem;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;

final class JPAFoundItem extends BaseFoundItem implements IFoundItem {

	JPAFoundItem(Item item) {
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
	public Object getAttributeValue(ItemAttribute attribute) {
		throw new UnsupportedOperationException("TODO");
	}
}
