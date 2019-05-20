package com.test.salesportal.index;

import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttribute;
import com.test.salesportal.search.BaseSearchItem;
import com.test.salesportal.search.FieldValues;

public class IndexSearchItem extends BaseSearchItem {

	public IndexSearchItem(
			String id,
			String title,
			Integer thumbWidth,
			Integer thumbHeight,
			FieldValues<SortAttribute> sortAttributeValues,
			FieldValues<ItemAttribute> fieldAttributeValues) {
		
		super(id, title, thumbWidth, thumbHeight, sortAttributeValues, fieldAttributeValues);
	}
}
