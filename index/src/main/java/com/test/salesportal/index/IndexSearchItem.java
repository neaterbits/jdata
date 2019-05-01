package com.test.salesportal.index;

import com.test.salesportal.search.BaseSearchItem;
import com.test.salesportal.search.FieldValues;

public class IndexSearchItem extends BaseSearchItem {

	public IndexSearchItem(String id, String title, Integer thumbWidth, Integer thumbHeight, FieldValues attributeValues) {
		super(id, title, thumbWidth, thumbHeight, attributeValues);
	}
}
