package com.test.cv.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;

import com.test.cv.common.IOUtil;
import com.test.cv.common.ItemId;
import com.test.cv.dao.IFoundItem;
import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.dao.criteria.Criterium;

public class SearchService extends BaseService {
	
	private static ISearchDAO getSearchDAO(HttpServletRequest request) {
		
		final ISearchDAO ret;
		
		final Storage storage = getStorageType(request);
		
		switch (storage) {
		case LOCAL_FILE_LUCENE:
			ret = null; // TODO Lucene DAO new XMLItemDAO(getLocalXMLStorage());
			break;
			
		default:
			throw new UnsupportedOperationException("TODO - unsupported storage " + storage);
		}
		
		return ret;
	}
	
	@POST
	// TODO check that we adhere to best practices for pageNo and itemsPerPage
	public SearchResult search(String freeText, String [] types, SearchCriterium [] criteria, Integer pageNo, Integer itemsPerPage, HttpServletRequest request) {
		
		final Criterium [] daoCriteria; 
		if (criteria != null) {
			daoCriteria = new Criterium[criteria.length];
		}
		else {
			daoCriteria = null;
		}
		
		final ISearchCursor cursor = getSearchDAO(request).search(null, daoCriteria);
		
		final int totalMatchCount = cursor.getTotalMatchCount();
		
		final int initialIdx;
		final int count;
		if (pageNo != null && itemsPerPage != null) {
			if (pageNo < 1) {
				throw new IllegalArgumentException("pageNo < 1");
			}
			
			initialIdx = itemsPerPage * (pageNo - 1); // starts at 1
			count = itemsPerPage;
		}
		else {
			// return all results
			initialIdx = 0;
			count = totalMatchCount;
		}
		
		final List<IFoundItem> found = cursor.getItems(initialIdx, count);
		
		final SearchResult result = new SearchResult();
		
		final int numFound = found.size();
		
		result.setPageFirstItem(initialIdx);
		result.setPageItemCount(numFound);
		result.setTotalItemMatchCount(totalMatchCount);
		
		final SearchItemResult [] items = new SearchItemResult[numFound];
		
		for (int i = 0; i < numFound; ++ i) {
			final IFoundItem foundItem = found.get(i);

			items[i] = new SearchItemResult(foundItem.getItemId(), foundItem.getTitle());
		}
		
		result.setItems(items);

		return result;
	}
	
	// Get item thumbnails as one big compressed JPEG? Or as a stream of JPEGs?
	public byte [] getThumbnails(String userId, String [] itemIds, HttpServletRequest request) {

		final ItemId [] array = new ItemId[itemIds.length];

		for (int i = 0; i < itemIds.length; ++i) {
			array[i] = new ItemId(userId, itemIds[i]);
		}

		// Return thumbnails as concatenated JPEGs
		InputStream inputStream = null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(50000);

		try {
			inputStream = getItemDAO(request).retrieveAndConcatenateThumbnails(array);

			IOUtil.copyStreams(inputStream, baos);
		} catch (ItemStorageException ex) {
			throw new IllegalStateException("Could not get thumbnails");
		} catch (IOException ex) {
			throw new IllegalStateException("Could not read thumbnails");
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}

		return baos.toByteArray();
	}
}
