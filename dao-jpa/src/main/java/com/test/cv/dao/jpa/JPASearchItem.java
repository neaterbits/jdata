package com.test.cv.dao.jpa;

import com.test.cv.dao.BaseSearchItem;
import com.test.cv.dao.SearchItem;

public class JPASearchItem extends BaseSearchItem implements SearchItem {

	public JPASearchItem(String id, String title, Integer thumbWidth, Integer thumbHeight) {
		super(id, title, thumbWidth, thumbHeight);
	}
}
