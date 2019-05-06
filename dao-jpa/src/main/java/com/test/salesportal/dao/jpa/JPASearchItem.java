package com.test.salesportal.dao.jpa;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.search.AttributeValues;
import com.test.salesportal.search.BaseSearchItem;
import com.test.salesportal.search.SearchItem;

final class JPASearchItem extends BaseSearchItem implements SearchItem {

	static JPASearchItem mapFromRow(Object [] row) {

		return new JPASearchItem(
				String.valueOf((Long)row[0]),
				(String)row[1],
				(Integer)row[2],
				(Integer)row[3],
				null,
				null); // TODO
	}
	
	JPASearchItem(
			String id,
			String title,
			Integer thumbWidth,
			Integer thumbHeight,
			AttributeValues<SortAttribute> sortValues,
			AttributeValues<ItemAttribute> fieldValues) {
		
		super(id, title, thumbWidth, thumbHeight, null, fieldValues);
	}

	public JPASearchItem(Item item) {
		this(String.valueOf(item.getId()), item.getTitle(), item.getThumbWidth(), item.getThumbHeight(), null, null);
	}
}
