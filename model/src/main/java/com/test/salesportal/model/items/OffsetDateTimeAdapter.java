package com.test.salesportal.model.items;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class OffsetDateTimeAdapter extends XmlAdapter<String, OffsetDateTime> {

	private static final DateTimeFormatter TZDATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	
	@Override
	public String marshal(OffsetDateTime v) throws Exception {
		final String value =  v.format(TZDATE_FORMATTER);
		
		return value;
	}

	@Override
	public OffsetDateTime unmarshal(String v) throws Exception {
		return OffsetDateTime.parse(v, TZDATE_FORMATTER);
	}
}
