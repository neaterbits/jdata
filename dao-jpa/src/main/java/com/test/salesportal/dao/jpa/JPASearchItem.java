package com.test.salesportal.dao.jpa;

import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttribute;
import com.test.salesportal.model.items.base.TitlePhotoItem;
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

	public JPASearchItem(TitlePhotoItem item) {
		this(String.valueOf(item.getId()), item.getTitle(), item.getThumbWidth(), item.getThumbHeight(), null, null);
	}
}
