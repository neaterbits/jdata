package com.test.salesportal.rest.search.all.cache;

import java.util.Comparator;

import com.test.salesportal.model.items.ItemAttribute;

class SearchRangeUtil {
	static boolean matches(
			Object valueObject,
			ItemAttribute attribute,
			Object lower,
			boolean includeLower,
			Object upper,
			boolean includeUpper) {
		
		final Comparator<Object> comparator = attribute.getAttributeType().makeObjectValueComparator();
		
		final boolean matches;
		
		if (lower == null && upper == null) {
			throw new IllegalArgumentException();
		}
		else if (lower == null) {
			
			final int comparison = comparator.compare(valueObject, upper);
			
			matches = includeUpper
					? comparison <= 0
					: comparison < 0;
		}
		else if (upper == null) {

			final int comparison = comparator.compare(valueObject, lower);
			
			matches = includeLower
					? comparison >= 0
					: comparison > 0;
		}
		else {
			final int lowerComparison = comparator.compare(valueObject, lower);
			final int upperComparison = comparator.compare(valueObject, upper);
			
			matches =
					   (includeLower ? lowerComparison >= 0 : lowerComparison > 0)
					&& (includeUpper ? upperComparison <= 0 : upperComparison < 0);
		}
		
		return matches;
	}
}
