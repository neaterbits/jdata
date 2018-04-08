package com.test.cv.search.facets;

import java.util.Comparator;
import java.util.List;

import com.test.cv.model.ItemAttribute;

public class SortUtils {

	public interface SortFunctions<D> {
		Comparable<?> getValue(D document, ItemAttribute attribute);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int compareValues(Comparable<?> c1, Comparable<?> c2) {
		int result;
		
		if (c1 == null && c2 == null) {
			result = 0;
		}
		else if (c1 == null && c2 != null) {
			result = -1;
		}
		else if (c1 != null && c2 == null) {
			result = 1;
		}
		else {
			result = ((Comparable)c1).compareTo((Comparable)c2);
		}

		return result;
	}
	
	public static <D> Comparator<D> makeSortItemsComparator(List<ItemAttribute> sortOrder, SortFunctions<D> sortFunctions) {

		final Comparator<D> comparator;
		
		if (sortOrder == null || sortOrder.isEmpty()) {
			comparator = null;
		}
		else if (sortOrder.size() == 1) {
			final ItemAttribute attribute = sortOrder.get(0);

			comparator = (d1, d2) -> compareValues(sortFunctions.getValue(d1, attribute), sortFunctions.getValue(d2, attribute));
		}
		else {
			comparator = new Comparator<D>() {

				@Override
				public int compare(D d1, D d2) {

					int result = 0;
					
					for (ItemAttribute attribute : sortOrder) {
						final int value = compareValues(sortFunctions.getValue(d1, attribute), sortFunctions.getValue(d2, attribute));
						
						if (value != 0) {
							result = value;
							break;
						}
					}
					
					return result;
				}
			};
		}

		return comparator;
	}
}
