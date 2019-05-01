package com.test.salesportal.search.criteria;

import java.math.BigDecimal;

public class DecimalRange extends Range<BigDecimal> {

	public DecimalRange(BigDecimal lowerValue, boolean includeLower, BigDecimal upperValue, boolean includeUpper) {
		super(lowerValue, includeLower, upperValue, includeUpper);
	}
}
