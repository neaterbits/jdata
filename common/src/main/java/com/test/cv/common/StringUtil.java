package com.test.cv.common;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
	public static String [] split(String s, char splitChar) {
		
		int lastSplit = -1;
		final List<String> strings = new ArrayList<>();
		
		for (int i = 0; i < s.length(); ++ i) {
			final char c = s.charAt(i);
			
			if (c == splitChar) {
				if (i != 0 && lastSplit < i - 1) {
					strings.add(s.substring(lastSplit + 1, i));
					lastSplit = i;
				}
			}
		}
		
		if (lastSplit == -1) {
			strings.add(s);
		}
		else if (lastSplit < s.length() - 1) {
			strings.add(s.substring(lastSplit + 1, s.length()));
		}
		
		return strings.toArray(new String[strings.size()]);
	}
}
