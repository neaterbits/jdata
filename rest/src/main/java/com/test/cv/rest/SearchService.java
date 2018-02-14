package com.test.cv.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;

import com.test.cv.dao.IFoundItem;
import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.criteria.Criterium;
import com.test.cv.dao.xml.XMLItemDAO;
import com.test.cv.model.ItemPhotoThumbnail;
import com.test.cv.rest.BaseService.Storage;

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
	public byte [] getThumbnails(String [] itemIds, HttpServletRequest request) {
		
		final List<ItemPhotoThumbnail> thumbnails = getItemDAO(request).getThumbnails(itemIds);

		// Return thumbnails as concatenated JPEGs
		return getItemDAO(request).retrieveAndConcatenateThumbnails(itemIds);
	}
}
