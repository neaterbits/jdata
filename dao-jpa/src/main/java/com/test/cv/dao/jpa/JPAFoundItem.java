package com.test.cv.dao.jpa;


import com.test.cv.dao.BaseFoundItem;
import com.test.cv.dao.IFoundItem;
import com.test.cv.model.Item;

final class JPAFoundItem extends BaseFoundItem implements IFoundItem {

	JPAFoundItem(Item item) {
		super(item);
	}
}
