package com.test.salesportal.rest.search.all.cache;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.Assertions;

import com.test.salesportal.model.SortOrder;
import com.test.salesportal.rest.search.all.cache.SimpleSortedSearchResult;

import junit.framework.TestCase;

public class SimpleSortedSearchResultTest extends TestCase {

	private static final Integer [] EMPTY_ARRAY = new Integer[0];
	
	public void testGetFromArray() {

		Integer [] array = new Integer[0];
		
		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 1, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(EMPTY_ARRAY);
		
		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 1, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(EMPTY_ARRAY);
		
		array = new Integer[] { 1, 2, 3, 4, 5};
		
		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 1, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 1 });
		
		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 1, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 5 });

		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 2, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 1, 2 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 2, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 5, 4 });

		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 3, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 1, 2, 3 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 3, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 5, 4, 3 });

		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 4, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 1, 2, 3, 4 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 4, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 5, 4, 3, 2 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 5, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 1, 2, 3, 4, 5 });

		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 5, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 5, 4, 3, 2, 1 });
		
		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 6, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 1, 2, 3, 4, 5 });

		assertThat(SimpleSortedSearchResult.getFromArray(array, 0, 6, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 5, 4, 3, 2, 1 });

		assertThat(SimpleSortedSearchResult.getFromArray(array, 1, 1, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 2 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 1, 1, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 4 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 1, 2, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 2, 3 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 1, 2, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 4, 3 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 1, 3, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 2, 3, 4 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 1, 3, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 4, 3, 2 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 1, 4, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 2, 3, 4, 5 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 1, 4, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 4, 3, 2, 1 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 1, 5, SortOrder.ASCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 2, 3, 4, 5 });
	
		assertThat(SimpleSortedSearchResult.getFromArray(array, 1, 5, SortOrder.DESCENDING, EMPTY_ARRAY))
			.isEqualTo(new Integer [] { 4, 3, 2, 1 });
	}
	
	public void testReverse() {
		
		Integer [] array = new Integer[] { 1, 2, 3, 4, 5 };
		SimpleSortedSearchResult.reverse(array);
		assertThat(array).isEqualTo(new Integer [] { 5, 4, 3, 2, 1 });

		array = new Integer[] { 1, 2, 3, 4 };
		SimpleSortedSearchResult.reverse(array);
		assertThat(array).isEqualTo(new Integer [] { 4, 3, 2, 1 });
		
		array = new Integer[] { 1, 2, 3 };
		SimpleSortedSearchResult.reverse(array);
		assertThat(array).isEqualTo(new Integer [] { 3, 2, 1 });

		array = new Integer[] { 1, 2 };
		SimpleSortedSearchResult.reverse(array);
		assertThat(array).isEqualTo(new Integer [] { 2, 1 });

		array = new Integer[] { 1 };
		SimpleSortedSearchResult.reverse(array);
		assertThat(array).isEqualTo(new Integer [] { 1 });

		array = new Integer[] { };
		SimpleSortedSearchResult.reverse(array);
		assertThat(array).isEqualTo(new Integer [] { });

	}
	
	public void testInsertArray() {
		
		Integer [] array = new Integer[] { 1, 2, 3};

		assertThat(SimpleSortedSearchResult.insertAt(array, 0, 12, Integer[]::new))
			.isEqualTo(new Integer [] { 12, 1, 2, 3 });
		
		assertThat(SimpleSortedSearchResult.insertAt(array, 1, 12, Integer[]::new))
			.isEqualTo(new Integer [] { 1, 12, 2, 3 });
		
		assertThat(SimpleSortedSearchResult.insertAt(array, 2, 12, Integer[]::new))
			.isEqualTo(new Integer [] { 1, 2, 12, 3 });
		
		try {
			SimpleSortedSearchResult.insertAt(array, 3, 12, Integer[]::new);
			
			Assertions.fail("Expected exception");
		}
		catch (IllegalArgumentException ex) {
			
		}
	}
	
	public void testAppendToArray() {
		
		Integer [] array = new Integer[] { 1, 2, 3};

		assertThat(SimpleSortedSearchResult.append(new Integer[0], 12))
			.isEqualTo(new Integer [] { 12 });
		
		assertThat(SimpleSortedSearchResult.append(array, 12))
			.isEqualTo(new Integer [] { 1, 2, 3, 12 });
	}
}
