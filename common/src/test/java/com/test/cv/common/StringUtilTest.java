package com.test.cv.common;

import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilTest {

	public void testSplit() {
		final String [] parts = StringUtil.split("this.is.a.string", '.');
		
		assertThat(parts.length).isEqualTo(4);
		
		assertThat(parts[0]).isEqualTo("this");
		assertThat(parts[1]).isEqualTo("is");
		assertThat(parts[2]).isEqualTo("a");
		assertThat(parts[3]).isEqualTo("string");
	}
}
