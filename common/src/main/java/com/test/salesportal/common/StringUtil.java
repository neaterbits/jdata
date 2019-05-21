package com.test.salesportal.common;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
	
	public static String [] split(String s, char splitChar) {
		
		int lastSplit = -1;
		final List<String> strings = new ArrayList<>();
		
		for (int i = 0; i < s.length(); ++ i) {
			final char c = s.charAt(i);
			
			if (c == splitChar) {
				if (i == 0) {
					strings.add("");
					lastSplit = 0;
				}
				else {
					strings.add(s.substring(lastSplit + 1, i));
					lastSplit = i;
				}
			}
		}
		
		if (lastSplit == -1) {
			strings.add(s);
		}
		else if (lastSplit < s.length()) {
			strings.add(s.substring(lastSplit + 1, s.length()));
		}
		
		return strings.toArray(new String[strings.size()]);
	}
	
	public static String join(String [] strings, char joinChar) {
		
		if (strings == null) {
			throw new IllegalArgumentException("strings == null");
		}
		
		final String result;
		
		if (strings.length == 0) {
			result = "";
		}
		else {
			
			final StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < strings.length; ++ i) {
				if (i > 0) {
					sb.append(joinChar);
				}
				
				sb.append(strings[i]);
			}
			
			result = sb.toString();
		}
		
		return result;
	}
	
	public static String trimToNull(String text) {
		
		final String result;
		
		if (text == null) {
			result = null;
		}
		else {
			final String trimmed = text.trim();
			
			result = trimmed.isEmpty() ? null : trimmed;
		}

		return result;
	}
	
	public static boolean containsWholeWord(String toScan, String toScanFor, boolean caseSensitive) {

		if (toScan == null) {
			throw new IllegalArgumentException("toScan == null");
		}
		
		if (toScanFor == null) {
			throw new IllegalArgumentException("toScanFor == null");
		}
		
		boolean mayHaveWholeWordMatch = true;
		
		boolean contains;
		
		final String trimmed = toScanFor.trim();

		if (trimmed.length() > toScan.length()) {
			contains = false;
		}
		else {
			final int maxIndex = toScan.length() - trimmed.length();
			
			contains = false;
			
			for (int i = 0; i <= maxIndex; ++ i) {
				
				if (mayHaveWholeWordMatch) {
					
					boolean matches = true;

					int toScanIdx = i;
					int toScanForIdx = 0;
					
					for (;;) {

						char c1 = 0;
						char c2 = 0;

						while (toScanForIdx < trimmed.length() && Character.isWhitespace(c2 = trimmed.charAt(toScanForIdx))) {
							++ toScanForIdx;
						}
						
						if (toScanForIdx >= trimmed.length()) {
							break;
						}
						
						while (toScanIdx < toScan.length() && Character.isWhitespace(c1 = toScan.charAt(toScanIdx))) {
							++ toScanIdx;
						}
						
						if (toScanIdx >= toScan.length()) {
							matches = false;
							break;
						}

						if (caseSensitive) {
							if (c1 != c2) {
								matches = false;
								break;
							}
						}
						else {
							if (Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
								matches = false;
								break;
							}
						}
						
						++ toScanIdx;
						++ toScanForIdx;
					}
					
					if (matches) {
						contains = true;
						break;
					}
					else {
						mayHaveWholeWordMatch = Character.isWhitespace(toScan.charAt(i));
					}
				}
				else {
					mayHaveWholeWordMatch = Character.isWhitespace(toScan.charAt(i));
				}
			}
		}
		
		return contains;
	}
}
