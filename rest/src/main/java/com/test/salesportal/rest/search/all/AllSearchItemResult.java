package com.test.salesportal.rest.search.all;

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

		this.modelVersion = modelVersion;
	}

	public long getModelVersion() {
		return modelVersion;
	}
}
