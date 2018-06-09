package com.test.cv.index;

import com.test.cv.search.BaseSearchItem;
import com.test.cv.search.FieldValues;

public class IndexSearchItem extends BaseSearchItem {

	public IndexSearchItem(String id, String title, Integer thumbWidth, Integer thumbHeight, FieldValues attributeValues) {
		super(id, title, thumbWidth, thumbHeight, attributeValues);
	}
}
