package com.test.salesportal.search.criteria;

public class IntegerRange extends Range<Integer> {

	public IntegerRange(Integer lowerValue, boolean includeLower, Integer upperValue, boolean includeUpper) {
		super(lowerValue, includeLower, upperValue, includeUpper);
	}
}
