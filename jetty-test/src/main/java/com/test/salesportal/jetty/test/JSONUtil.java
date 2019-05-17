package com.test.salesportal.jetty.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class JSONUtil {

	static ObjectMapper createMapper() {

		final ObjectMapper mapper = new ObjectMapper();
		
		mapper.registerModule(new JavaTimeModule())
			.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

		mapper.setDateFormat(new StdDateFormat());

		return mapper;
	}
	
	static <T> T decodeJson(byte [] data, Class<T> type) throws IOException {
		
		return createMapper().readValue(new ByteArrayInputStream(data), type);
	}

	static <T> void encodeJson(T item, OutputStream outputStream) throws IOException {
		
		createMapper().writeValue(outputStream, item);
	}
}
