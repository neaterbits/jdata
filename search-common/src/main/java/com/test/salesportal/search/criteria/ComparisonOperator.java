package com.test.salesportal.search.criteria;

public enum ComparisonOperator {
	EQUALS("="),
	NOT_EQUALS("<>"),
	LESS_THAN("<"),
	LESS_THAN_OR_EQUALS("<="),
	GREATER_THAN(">"),
	GREATER_THAN_OR_EQUALS(">=");
	
	private final String mathString;

	private ComparisonOperator(String mathString) {
		this.mathString = mathString;
	}

	public String getMathString() {
		return mathString;
	}
}
