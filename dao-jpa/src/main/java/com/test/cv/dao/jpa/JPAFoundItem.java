package com.test.cv.dao.jpa;


import com.test.cv.dao.BaseFoundItem;
import com.test.cv.dao.IFoundItem;
import com.test.cv.model.Item;

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
}
