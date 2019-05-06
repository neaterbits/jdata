package com.test.salesportal.rest.search.all.cache;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.PropertyAttribute;
import com.test.salesportal.model.SortAttribute;
import com.test.salesportal.model.SortOrder;
import com.test.salesportal.rest.search.SearchItemResult;
import com.test.salesportal.rest.search.all.AllSearchItemResult;

/**
 * Simple but slow sorted search result
 */

public final class SimpleSortedSearchResult extends SortedSearchResult {

	private volatile AllSearchItemResult [] items;
	
	public SimpleSortedSearchResult(String searchResultId, List<SortAttribute> sortAttributes) {
		super(searchResultId, sortAttributes);
	}

	@Override
	void insertSearchResults(AllSearchItemResult[] searchResults) {

		if (searchResults == null || searchResults.length == 0) {
			throw new IllegalArgumentException();
		}

		for (SearchItemResult searchItemResult : searchResults) {
			if (searchItemResult.getFields() == null) {
				throw new IllegalArgumentException("searchItemResult.getFields() == null");
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
		
		Arrays.sort(updatedItems, makeSortAttributeComparator());
		
		// No need to synchronize since threads will see old array
		this.items = updatedItems;
	}
	
	private Comparator<SearchItemResult> makeSortAttributeComparator() {
		
		final List<SortAttribute> sortAttributes = getSortAttributes();

		return new Comparator<SearchItemResult>() {

			@Override
			public int compare(SearchItemResult o1, SearchItemResult o2) {
				
				int compareResult = 0;
				
				for (int i = 0; i < sortAttributes.size(); ++ i) {
					final SortAttribute sortAttribute = sortAttributes.get(i);
					
					final Object fieldValue1 = o1.getSortFields()[i];
					final Object fieldValue2 = o2.getSortFields()[i];
					
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
		};
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


	@Override
	void applyItemToCachedItems(SearchKey searchKey, Item item) {

		// Figure out whether already cached and apply, or create new item at pos in sort order
		final String itemId = item.getIdString();
		
		// Check if matches search criteria
		if (SearchKeyMatchUtil.matchesSearchKey(searchKey, item)) {

			// Matches, this might be an existing entry or a new one
			int itemIndex = -1;
			
			int insertionPoint = -1;
			
			final SearchItemResult [] toSearch = items; // in case of threaded update
			
			final Comparator<SearchItemResult> comparator = makeSortAttributeComparator();
			
			final AllSearchItemResult updatedItemResult = makeSearchItemResult(searchKey, item);
			
			for (int i = 0; i < toSearch.length; ++ i) {
				
				final SearchItemResult searchItemResult = toSearch[i];
				
				if (insertionPoint == -1 && comparator.compare(updatedItemResult, searchItemResult) > 0) {
					
					// Last element?
					if (i < toSearch.length - 1) {
						// No, can insert before next
						insertionPoint = i + 1;
					}
				}
				
				if (itemId.equals(searchItemResult.getId())) {
					if (itemIndex != -1) {
						throw new IllegalStateException();
					}
					
					itemIndex = i;
				}
			}
			
			if (itemIndex == -1) {
				// Not yet added
				if (insertionPoint != -1) {
					this.items = insertAt(items, insertionPoint, updatedItemResult, AllSearchItemResult[]::new);
				}
				else {
					this.items = append(items, updatedItemResult);
				}
			}
			else {
				// Already added and matches, update search values
				this.items[itemIndex] = updatedItemResult;
			}
		}
		else {
			deleteItem(itemId);
		}

	}
	
	private static AllSearchItemResult makeSearchItemResult(SearchKey searchKey, Item item) {
	
		return new AllSearchItemResult(
				item.getModelVersion(),
				item.getIdString(),
				item.getTitle(),
				item.getThumbWidth(),
				item.getThumbHeight(),
				getSortValues(searchKey.getSortAttributes(), item),
				getFieldValues(searchKey.getFieldAttributes(), item));
		
			
	}

	private static Object [] getSortValues(List<SortAttribute> fieldAttributes, Item fieldItem) {
		return getFieldValues(fieldAttributes, fieldItem, SortAttribute::getObjectValue);
	}

	private static Object [] getFieldValues(List<ItemAttribute> fieldAttributes, Item fieldItem) {
		return getFieldValues(fieldAttributes, fieldItem, ItemAttribute::getObjectValue);
	}
		
	private static <T extends PropertyAttribute>
	Object [] getFieldValues(List<T> fieldAttributes, Item item, BiFunction<T, Item, Object> getObjectValue) {
		
		final int numFields = fieldAttributes != null
				? fieldAttributes.size()
				: 0;
		
		final Object [] fieldObjects;
		
		if (numFields > 0) {
			
			fieldObjects = new Object[numFields];
			
			for (int i = 0; i < numFields; ++ i) {
				fieldObjects[i] = getObjectValue.apply(fieldAttributes.get(i), item);
			}
		}
		else {
			fieldObjects = null;
		}

		return fieldObjects;
	}

	@Override
	void deleteItemFromCachedItems(String itemId) {
		
		int matchingIndex = -1;
		
		for (int i = 0; i < items.length; ++ i) {
			if (itemId.equals(items[i].getId())) {
				
				if (matchingIndex != -1) {
					throw new IllegalStateException();
				}
				
				matchingIndex = i;
			}
		}
		
		if (matchingIndex != -1) {
			this.items = removeFromArray(items, matchingIndex, AllSearchItemResult[]::new);
		}
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

	@FunctionalInterface
	interface NewArray<T> {
		T [] createArray(int length);
	}
	
	static <T> T [] removeFromArray(T [] array, int index, NewArray<T> createArray) {
		
		if (array == null) {
			throw new IllegalArgumentException();
		}
		
		if (array.length == 0) {
			throw new IllegalArgumentException();
		}
		
		if (index >= array.length) {
			throw new IllegalArgumentException();
		}

		final T [] result;
		
		if (index == 0) {
			result = Arrays.copyOfRange(array, 1, array.length);
		}
		else if (index == array.length - 1) {
			result = Arrays.copyOfRange(array, array.length - 1, array.length);
		}
		else {
			result = createArray.createArray(array.length - 1);
			
			System.arraycopy(array, 0, result, 0, index);
			System.arraycopy(array, index + 1, result, index, array.length - index - 1);
		}
		
		return result;
	}
	
	static <T> T [] insertAt(T [] array, int index, T element, NewArray<T> createArray) {

		if (array == null) {
			throw new IllegalArgumentException("array == null");
		}
		
		if (index < 0) {
			throw new IllegalArgumentException("index < 0");
		}
		
		if (index >= array.length) {
			throw new IllegalArgumentException("index >= array.length");
		}
		
		if (element == null) {
			throw new IllegalArgumentException("element == null");
		}
		
		final T [] updated = createArray.createArray(array.length + 1);
		
		System.arraycopy(array, 0, updated, 0, index);
		
		updated[index] = element;
		
		System.arraycopy(array, index, updated, index + 1, array.length - index);
		
		return updated;
	}
	
	static <T> T[] append(T [] array, T element) {
		
		if (array == null) {
			throw new IllegalArgumentException("array == null");
		}

		if (element == null) {
			throw new IllegalArgumentException("element == null");
		}

		final T [] updated = Arrays.copyOf(array, array.length + 1);
		
		updated[array.length] = element;
	
		return updated;
	}
 }
