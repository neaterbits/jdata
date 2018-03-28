package com.test.cv.xmlstorage.api;

import static org.assertj.core.api.Assertions.assertThat;

import junit.framework.TestCase;

public class BaseXMLStorageTest extends TestCase {

	public void testSplit() {
		final String [] parts = BaseXMLStorage.split("this.is.a.string", '.');
		
		assertThat(parts.length).isEqualTo(4);
		
		assertThat(parts[0]).isEqualTo("this");
		assertThat(parts[1]).isEqualTo("is");
		assertThat(parts[2]).isEqualTo("a");
		assertThat(parts[3]).isEqualTo("string");
	}
}
