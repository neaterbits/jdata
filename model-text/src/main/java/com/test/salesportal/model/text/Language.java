package com.test.salesportal.model.text;

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
	
	public static Language fromCode(String code) {
		for (Language language : values()) {
			if (language.code.equals(code)) {
				return language;
			}
		}
		
		return null;
	}
}
