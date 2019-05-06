package com.test.salesportal.rest.search.all;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.model.SortOrder;
import com.test.salesportal.rest.search.SearchItemResult;

/**
 * Simple but slow sorted search result
 */

final class SimpleSortedSearchResult extends SortedSearchResult {

	private volatile AllSearchItemResult [] items;
	
	SimpleSortedSearchResult(String searchResultId, List<SortAttribute> sortAttributes, int indexIntoFields) {
		super(searchResultId, sortAttributes, indexIntoFields);
	}

	@Override
	void insertSearchResults(AllSearchItemResult[] searchResults) {

		if (searchResults == null || searchResults.length == 0) {
			throw new IllegalArgumentException();
		}

		final List<SortAttribute> sortAttributes = getSortAttributes();
		final int indexIntoFields = getIndexIntoFields();
		
		for (SearchItemResult searchItemResult : searchResults) {
			if (searchItemResult.getFields() == null) {
				throw new IllegalArgumentException("searchItemResult.getFields() == null");
			}
			
			if (searchItemResult.getFields().length < indexIntoFields + sortAttributes.size()) {
				throw new IllegalArgumentException("Not enough fields in result");
			}
		}

		final AllSearchItemResult [] updatedItems;
		
		if (items == null) {
			updatedItems = Arrays.copyOf(searchResults, searchResults.length);
		}
		else {
			final int length = items.length;
			final int newLength = items.length + searchResults.length;
			
			updatedItems = Arrays.copyOf(items, newLength);
			
			for (int i = 0; i < searchResults.length; ++ i) {
				updatedItems[length + i] = searchResults[i];
			}
		}
		
		Arrays.sort(updatedItems, new Comparator<SearchItemResult>() {

			@Override
			public int compare(SearchItemResult o1, SearchItemResult o2) {
				
				int compareResult = 0;
				
				for (int i = 0; i < sortAttributes.size(); ++ i) {
					final SortAttribute sortAttribute = sortAttributes.get(i);
					
					final int fieldIdx = indexIntoFields + i;
					
					final Object fieldValue1 = o1.getFields()[fieldIdx];
					final Object fieldValue2 = o2.getFields()[fieldIdx];
					
					switch (sortAttribute.getAttributeType()) {
					case STRING:
						compareResult = String.CASE_INSENSITIVE_ORDER.compare((String)fieldValue1, (String)fieldValue2);
						break;
						
					default:
						throw new UnsupportedOperationException("Unsupported attribute type " + sortAttribute.getAttributeType());
					}
					
					if (compareResult != 0) {
						break;
					}
				}
				
				
				return compareResult;
			}
		});
		
		// No need to synchronize since threads will see old array
		this.items = updatedItems;
	}

	
	@Override
	AllSearchItemResult[] getSearchResults(int index, int count, SortOrder sortOrder) {

		if (index < 0) {
			throw new IllegalArgumentException("index < 0");
		}
		
		if (count < 0) {
			throw new IllegalArgumentException("count < 0");
		}
		
		return getFromArray(items, index, count, sortOrder, EMPTY_ARRAY);
	}
	
	@Override
	int getMatchCount() {
		
		if (items == null) {
			throw new IllegalStateException();
		}
		
		return items.length;
	}

	static <T> T [] getFromArray(T [] items, int index, int count, SortOrder sortOrder, T [] emptyArray) {
		
		final T [] result;
		
		switch (sortOrder) {
		case ASCENDING:
			if (index >= items.length) {
				result = emptyArray;
			}
			else if (index + count >= items.length) {
				result = Arrays.copyOfRange(items, index, items.length);
			}
			else {
				result = Arrays.copyOfRange(items, index, index + count);
			}
			break;
			
		case DESCENDING:
			if (index >= items.length) {
				result = emptyArray;
			}
			else if (index + count >= items.length) {
				result = Arrays.copyOfRange(items, 0, items.length - index);
			}
			else {
				result = Arrays.copyOfRange(items, items.length - index - count, items.length - index);
			}
			
			reverse(result);
			break;
			
		default:
			throw new UnsupportedOperationException("Unknown sort order " + sortOrder);
		}
		
		return result;
	}
	
	static <T> void reverse(T [] array) {
		
		if (array.length > 0) {
			
			final int mid = array.length / 2;

			for (int i = 0; i < mid; ++ i) {
				final T start = array[i];
				
				final int endIdx = array.length - i - 1;
				final T end = array[endIdx];
				
				array[i] = end;
				array[endIdx] = start;
			}
		}
	}
}
