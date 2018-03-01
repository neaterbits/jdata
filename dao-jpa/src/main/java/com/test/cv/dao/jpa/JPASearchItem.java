package com.test.cv.dao.jpa;

import com.test.cv.model.Item;
import com.test.cv.search.BaseSearchItem;
import com.test.cv.search.SearchItem;

final class JPASearchItem extends BaseSearchItem implements SearchItem {

	static JPASearchItem mapFromRow(Object [] row) {

		return new JPASearchItem(
				String.valueOf((Long)row[0]),
				(String)row[1],
				(Integer)row[2],
				(Integer)row[3]);
	}

	JPASearchItem(String id, String title, Integer thumbWidth, Integer thumbHeight) {
		super(id, title, thumbWidth, thumbHeight);
	}

	public JPASearchItem(Item item) {
		this(String.valueOf(item.getId()), item.getTitle(), item.getThumbWidth(), item.getThumbHeight());
	}
}
