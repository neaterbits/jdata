package com.test.cv.rest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;

import com.test.cv.common.IOUtil;
import com.test.cv.common.ItemId;
import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.dao.SearchException;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.attributes.AttributeType;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.model.items.TypeInfo;
import com.test.cv.search.SearchItem;
import com.test.cv.search.criteria.ComparisonOperator;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.criteria.DecimalCriterium;
import com.test.cv.search.criteria.DecimalRangeCriterium;
import com.test.cv.search.criteria.IntegerCriterium;
import com.test.cv.search.criteria.IntegerRangeCriterium;
import com.test.cv.search.criteria.StringCriterium;
import com.test.cv.search.facets.IndexFacetedAttributeResult;
import com.test.cv.search.facets.IndexRangeFacetedAttributeResult;
import com.test.cv.search.facets.IndexSimpleFacetedAttributeResult;
import com.test.cv.search.facets.ItemsFacets;
import com.test.cv.search.facets.TypeFacets;

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
		
		final List<Criterium> daoCriteria; 
		if (criteria != null) {
			daoCriteria = convertCriteria(criteria);
		}
		else {
			daoCriteria = null;
		}

		// TODO support types
		final ISearchCursor cursor;
		try {
			cursor = getSearchDAO(request).search(null, daoCriteria, null);
		} catch (SearchException ex) {
			throw new IllegalStateException("Failed to search", ex);
		}

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
		
		final List<SearchItem> found = cursor.getItemIDsAndTitles(initialIdx, count);
		
		final SearchResult result = new SearchResult();
		
		final int numFound = found.size();
		
		result.setPageFirstItem(initialIdx);
		result.setPageItemCount(numFound);
		result.setTotalItemMatchCount(totalMatchCount);
		
		final SearchItemResult [] items = new SearchItemResult[numFound];
		
		if (cursor.getFacets() != null) {
			result.setFacets(convertFacets(cursor.getFacets()));
		}
		
		for (int i = 0; i < numFound; ++ i) {
			final SearchItem foundItem = found.get(i);

			items[i] = new SearchItemResult(foundItem.getItemId(), foundItem.getTitle(), foundItem.getThumbWidth(), foundItem.getThumbHeight());
		}
		
		result.setItems(items);

		return result;
	}
	
	private static List<Criterium> convertCriteria(SearchCriterium [] searchCriteria) {
		final List<Criterium> criteria = new ArrayList<>(searchCriteria.length);
		
		for (int i = 0; i < searchCriteria.length; ++ i) {
			criteria.add(convertCriterium(searchCriteria[i]));
		}

		return criteria;
	}
	
	private static Criterium convertCriterium(SearchCriterium searchCriterium) {
		
		// Figure out the type first
		final String typeName = searchCriterium.getType();
		
		// This is a Java type, look it up from the types list
		final TypeInfo type = ItemTypes.getTypeByName(typeName);
		
		if (type == null) {
			throw new IllegalArgumentException("Unknown type " + typeName);
		}
		
		// Find the attribute
		final ItemAttribute attribute = type.getAttributes().getByName(searchCriterium.getAttribute());
		
		if (attribute == null) {
			throw new IllegalArgumentException("Unknown attribute " + searchCriterium.getAttribute() + " from type " + typeName);
		}

		final Criterium criterium;
		
		final SearchRange range = searchCriterium.getRange();
		if (range != null) {
			
			switch (attribute.getAttributeType()) {
			case STRING:
				throw new UnsupportedOperationException("Range query for strings");
				
			case INTEGER:
				criterium = new IntegerRangeCriterium(
						attribute,
						(Integer)range.getLower(), range.includeLower(),
						(Integer)range.getUpper(), range.includeUpper());
				break;
				
			case DECIMAL:
				criterium = new DecimalRangeCriterium(
						attribute,
						(BigDecimal)range.getLower(), range.includeLower(),
						(BigDecimal)range.getUpper(), range.includeUpper());
				break;
				
			default:
				throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
			}
		}
		else {
			switch (attribute.getAttributeType()) {
			case STRING:
				criterium = new StringCriterium(attribute, (String)searchCriterium.getValue(), ComparisonOperator.EQUALS);
				break;

			case INTEGER:
				criterium = new IntegerCriterium(attribute, (Integer)searchCriterium.getValue(), ComparisonOperator.EQUALS);
				break;

			case DECIMAL:
				criterium = new DecimalCriterium(attribute, (BigDecimal)searchCriterium.getValue(), ComparisonOperator.EQUALS);
				break;

			default:
				throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
			}
			
		}

		return criterium;
	}
	
	private static SearchFacetsResult convertFacets(ItemsFacets facets) {
		final SearchFacetsResult result = new SearchFacetsResult();
		
		final List<SearchFacetedTypeResult> typeFacetsResult = new ArrayList<>(facets.getTypes().size());
		
		for (TypeFacets typeFacet : facets.getTypes()) {
			final SearchFacetedTypeResult typeResult = new SearchFacetedTypeResult();

			final List<SearchFacetedAttributeResult> facetAttributesResult = convertAttributeList(typeFacet.getAttributes());
			
			typeResult.setType(getTypeId(typeFacet.getType()));
			typeResult.setAttributes(facetAttributesResult);
			
			typeFacetsResult.add(typeResult);
		}

		return result;
	}
	
	private static List<SearchFacetedAttributeResult> convertAttributeList(List<IndexFacetedAttributeResult> attributes) {

		final List<SearchFacetedAttributeResult> facetAttributesResult = new ArrayList<>(attributes.size());

		for (IndexFacetedAttributeResult indexFacetedAttribute : attributes) {
			final SearchFacetedAttributeResult searchFacetedAttribute;

			if (indexFacetedAttribute instanceof IndexSimpleFacetedAttributeResult) {
				final IndexSimpleFacetedAttributeResult indexSimpleFacetedAttributeResult
						= (IndexSimpleFacetedAttributeResult)indexFacetedAttribute;
				
				final SearchSimpleFacetedAttributeResult searchSimpleFacetedAttribute = new SearchSimpleFacetedAttributeResult();
				
				if (indexSimpleFacetedAttributeResult.getSubFacets() != null) {
					searchSimpleFacetedAttribute.setSubAttributes(convertAttributeList(indexSimpleFacetedAttributeResult.getSubFacets()));
				}
				
				searchSimpleFacetedAttribute.setMatchCount(indexSimpleFacetedAttributeResult.getMatchCount());
				
				searchFacetedAttribute = searchSimpleFacetedAttribute;
			}
			else if (indexFacetedAttribute instanceof IndexRangeFacetedAttributeResult) {
				final IndexRangeFacetedAttributeResult indexRangeFacetedAttributeResult
						= (IndexRangeFacetedAttributeResult)indexFacetedAttribute;

				final ItemAttribute attribute = indexFacetedAttribute.getAttribute();
				final AttributeType attributeType = attribute.getAttributeType();
				
				final int [] matchCounts = indexRangeFacetedAttributeResult.getMatchCounts();
				
				final List<SearchFacetedAttributeRangeResult<?>> ranges = new ArrayList<>(matchCounts.length);
				
				// Convert match count for each range to REST response format
				// Response contains the ranges as well for ease of use from UI code
				switch (attributeType) {
				case INTEGER: {
					for (int i = 0; i < matchCounts.length; ++ i) {
						final SearchFacetedAttributeIntegerRangeResult searchRange = new SearchFacetedAttributeIntegerRangeResult();
						
						// Set range lower and upper
						searchRange.setLower(attribute.getIntegerRanges()[i].getLower());
						searchRange.setUpper(attribute.getIntegerRanges()[i].getUpper());
						searchRange.setMatchCount(matchCounts[i]);
						
						ranges.add(searchRange);
					}
					break;
				}
					
				case DECIMAL: {
					for (int i = 0; i < matchCounts.length; ++ i) {
						final SearchFacetedAttributeDecimalRangeResult searchRange = new SearchFacetedAttributeDecimalRangeResult();
						
						// Set range lower and upper
						searchRange.setLower(attribute.getDecimalRanges()[i].getLower());
						searchRange.setUpper(attribute.getDecimalRanges()[i].getUpper());
						searchRange.setMatchCount(matchCounts[i]);

						ranges.add(searchRange);
					}
					break;
				}
					
				default:
					throw new UnsupportedOperationException("Unknown attribute range type " + attributeType);
				}
				
				final SearchRangeFacetedAttributeResult rangeResult = new SearchRangeFacetedAttributeResult();
				
				rangeResult.setRanges(ranges);
				
				searchFacetedAttribute = rangeResult;
			}
			else {
				throw new UnsupportedOperationException("Unknown index faceted attribute result type " + indexFacetedAttribute.getClass());
			}

			searchFacetedAttribute.setId(indexFacetedAttribute.getAttribute().getName());
			searchFacetedAttribute.setName(indexFacetedAttribute.getAttribute().getDisplayName());
		}

		return facetAttributesResult;
	}
	
	public byte[] searchReturnCompressed(String freeText, String [] types, SearchCriterium [] criteria, Integer pageNo, Integer itemsPerPage, HttpServletRequest request) {
		// Return result as a compressed array (non JSON) of
		// - IDs, in order
		// titles, in order
		// thumbnail sizes (byte width, byte height), in order
		
		final SearchResult searchResult = this.search(freeText, types, criteria, pageNo, itemsPerPage, request);

		// Add information to compression stream
		
		// TODO compress
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		final OutputStream outputStream = baos; // TODO compression
		
		try (DataOutputStream dataOut = new DataOutputStream(outputStream)) {
		
			try {
				for (SearchItemResult item : searchResult.getItems()) {
					dataOut.writeUTF(item.getId());
				}
				
				for (SearchItemResult item : searchResult.getItems()) {
					dataOut.writeUTF(item.getTitle());
				}
				
				for (SearchItemResult item : searchResult.getItems()) {
					dataOut.writeByte(item.getThumbWidth());
					dataOut.writeByte(item.getThumbHeight());
				}
			}
			catch (IOException ex) {
				throw new IllegalStateException("Failed to write to output stream");
			}
		} catch (IOException ex) {
			throw new IllegalStateException("Exception while closing output stream", ex);
		}

		return baos.toByteArray();
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
