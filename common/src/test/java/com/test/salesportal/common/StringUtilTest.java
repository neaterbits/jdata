package com.test.salesportal.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

import com.test.salesportal.common.StringUtil;

import junit.framework.TestCase;

public class StringUtilTest extends TestCase {

	public void testSplit() {
		final String [] parts = StringUtil.split("this.is.a.string", '.');
		
		assertThat(parts.length).isEqualTo(4);
		
		assertThat(parts[0]).isEqualTo("this");
		assertThat(parts[1]).isEqualTo("is");
		assertThat(parts[2]).isEqualTo("a");
		assertThat(parts[3]).isEqualTo("string");
	}

	public void testSplitWthEmptyWord() {
		final String [] parts = StringUtil.split(".this.is..a.string.", '.');
		
		// assertThat(parts.length).isEqualTo(7);
		
		assertThat(parts[0]).isEqualTo("");
		assertThat(parts[1]).isEqualTo("this");
		assertThat(parts[2]).isEqualTo("is");
		assertThat(parts[3]).isEqualTo("");
		assertThat(parts[4]).isEqualTo("a");
		assertThat(parts[5]).isEqualTo("string");
		assertThat(parts[6]).isEqualTo("");
	}
	
	public void testContainsWholeWord() {
		
		assertThat(StringUtil.containsWholeWord("abc", "abc", true)).isTrue();
		assertThat(StringUtil.containsWholeWord("Abc", "abc", true)).isFalse();
		
		assertThat(StringUtil.containsWholeWord(" Abc xyz ", "Abc", true)).isTrue();
		assertThat(StringUtil.containsWholeWord(" Abc xyz ", "Abc xyz", true)).isTrue();
		assertThat(StringUtil.containsWholeWord(" Abc xyz ", "bc xyz", true)).isFalse();
		assertThat(StringUtil.containsWholeWord(" Abc xyz ", "bc xy", true)).isFalse();
		assertThat(StringUtil.containsWholeWord(" Abc xyz ", "xyz", true)).isTrue();

		assertThat(StringUtil.containsWholeWord(" Abc xyz ", "Abc  xyz", true)).isTrue();
		assertThat(StringUtil.containsWholeWord(" Abc  xyz ", "Abc xyz", true)).isTrue();

	}
	
	public void testContainsWholeWordInputCheck() {
		
		try {
			StringUtil.containsWholeWord("abc", null, true);
		
			Assertions.fail("Expected exception");
		}
		catch (IllegalArgumentException ex) {
			
		}

		try {
			StringUtil.containsWholeWord(null, "abc", true);
		
			Assertions.fail("Expected exception");
		}
		catch (IllegalArgumentException ex) {
			
		}
}

}
