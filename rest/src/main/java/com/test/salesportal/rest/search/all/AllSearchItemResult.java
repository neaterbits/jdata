package com.test.salesportal.rest.search.all;

import java.util.Arrays;

import com.test.salesportal.rest.search.SearchItemResult;

public class AllSearchItemResult extends SearchItemResult {

	private final long modelVersion;

	public AllSearchItemResult(
			long modelVersion,
			String id,
			String title,
			Integer thumbWidth, Integer thumbHeight,
			Object[] sortFields,
			Object[] fields) {
		
		super(id, title, thumbWidth, thumbHeight, sortFields, fields);

		if (sortFields == null) {
			throw new IllegalArgumentException("sortFields == null");
		}
		
		this.modelVersion = modelVersion;
	}

	public long getModelVersion() {
		return modelVersion;
	}

	@Override
	public String toString() {
		return "AllSearchItemResult [modelVersion=" + modelVersion + ", getId()=" + getId() + ", getTitle()="
				+ getTitle() + ", getThumbWidth()=" + getThumbWidth() + ", getThumbHeight()=" + getThumbHeight()
				+ ", getSortFields()=" + Arrays.toString(getSortFields()) + ", getFields()="
				+ Arrays.toString(getFields()) + "]";
	}
}
