package com.test.cv.model;

public enum Language {
	
	NB_NO("nb_NO"),
	EN_GB("en_GB");
	
	private final String code;

	private Language(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
