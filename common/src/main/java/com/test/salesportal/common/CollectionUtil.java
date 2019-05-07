package com.test.salesportal.common;

import java.util.Collection;
import java.util.function.Predicate;

public class CollectionUtil {

	public static <T> T find(Collection<T> collection, Predicate<T> test) {
		
		for (T value : collection) {
			
			if (test.test(value)) {
				return value;
			}
		}
		
		return null;
	}
}
