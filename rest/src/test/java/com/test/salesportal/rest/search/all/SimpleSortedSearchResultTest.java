package com.test.salesportal.rest.search.all;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.salesportal.model.SortOrder;

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
		
		Integer [] array = new Integer[] { 1, 2, 3, 4, 5};
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
}
