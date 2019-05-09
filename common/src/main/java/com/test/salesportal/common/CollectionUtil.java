package com.test.salesportal.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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
	
	public static <T> boolean has(Collection<T> collection, Predicate<T> test) {

		for (T value : collection) {
			
			if (test.test(value)) {
				return true;
			}
		}
		
		return false;
	}
	
	@SafeVarargs
	public static <T> Set<T> asSet(T ... values) {
		
		final HashSet<T> set = new HashSet<>(values.length);
		
		for (T value : values) {
			set.add(value);
		}
		
		return set;
	}
}
