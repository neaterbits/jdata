package com.test.cv.rest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.test.cv.common.IOUtil;
import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.dao.SearchException;
import com.test.cv.dao.index.IndexSearchDAO;
import com.test.cv.integrationtest.IntegrationTestHelper;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.attributes.AttributeType;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.model.items.TypeInfo;
import com.test.cv.search.SearchItem;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.criteria.DecimalInCriterium;
import com.test.cv.search.criteria.DecimalRange;
import com.test.cv.search.criteria.DecimalRangesCriterium;
import com.test.cv.search.criteria.EnumInCriterium;
import com.test.cv.search.criteria.IntegerInCriterium;
import com.test.cv.search.criteria.IntegerRange;
import com.test.cv.search.criteria.IntegerRangesCriterium;
import com.test.cv.search.criteria.StringInCriterium;
import com.test.cv.search.facets.IndexFacetedAttributeResult;
import com.test.cv.search.facets.IndexRangeFacetedAttributeResult;
import com.test.cv.search.facets.IndexSingleValueFacet;
import com.test.cv.search.facets.IndexSingleValueFacetedAttributeResult;
import com.test.cv.search.facets.ItemsFacets;
import com.test.cv.search.facets.TypeFacets;

@Path("/search")
public class SearchService extends BaseService {
	
	private ISearchDAO getSearchDAO(HttpServletRequest request) {
		
		final ISearchDAO ret;
		
		final Storage storage = getStorageType(request);
		
		switch (storage) {
		case LOCAL_FILE_LUCENE:
			File baseDir = (File)request.getSession().getAttribute("baseDir");
			
			if (baseDir == null) {
				baseDir = IntegrationTestHelper.makeBaseDir();
				
				request.getSession().setAttribute("baseDir", baseDir);
			}
			
			ret = new IndexSearchDAO(assureIndex(), false);
			break;
			
		default:
			throw new UnsupportedOperationException("TODO - unsupported storage " + storage);
		}
		
		return ret;
	}
	
	@GET
	@Path("search")
	// TODO check that we adhere to best practices for pageNo and itemsPerPage
	public SearchResult search(String freeText, String [] types, SearchCriterium [] criteria, Integer pageNo, Integer itemsPerPage, Boolean testdata, HttpServletRequest request) {

		// If no types passed, this is the initial search so pass all
		if (types == null) {
			// All types
			types = ItemTypes.getTypeNames();
		}

		final SearchResult result;
		if (testdata != null && testdata && isTest()) {
			// Return a hardcoded testresult for simple local testing
			result = makeTestResult();
		}
		else {
			result = searchInDB(freeText, types, criteria, pageNo, itemsPerPage, request);
		}

		return result;
	}

	private SearchResult searchInDB(String freeText, String [] types, SearchCriterium [] criteria, Integer pageNo, Integer itemsPerPage, HttpServletRequest request) {
		
		final List<Criterium> daoCriteria; 
		if (criteria != null) {
			daoCriteria = convertCriteria(criteria);
		}
		else {
			daoCriteria = null;
		}

		// TODO support types
		final SearchResult result = new SearchResult();
			
		ISearchDAO searchDAO = null;

		searchDAO = getSearchDAO(request);
		
		try {
			final ISearchCursor cursor;
			try {
				cursor = searchDAO.search(null, daoCriteria, ItemTypes.getFacetAttributes(types));
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
				
				items[i] = new SearchItemResult(
						foundItem.getItemId(),
						foundItem.getTitle(),
						foundItem.getThumbWidth() != null ? foundItem.getThumbWidth() : THUMBNAIL_MAX_SIZE,
						foundItem.getThumbHeight() != null ? foundItem.getThumbHeight() : THUMBNAIL_MAX_SIZE);
			}
			
			result.setItems(items);
			
		}
		finally {
			try {
				searchDAO.close();
			} catch (Exception ex) {
				throw new IllegalStateException("Failed to close search DAO", ex);
			}
		}

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
		
		final SearchRange [] ranges = searchCriterium.getRanges();
		if (ranges != null) {
			
			switch (attribute.getAttributeType()) {
			case STRING:
				throw new UnsupportedOperationException("Range query for strings");
				
			case INTEGER:
				final IntegerRange [] integerRanges = new IntegerRange[ranges.length];
				
				for (int i = 0; i < ranges.length; ++ i) {
					final SearchRange range = ranges[i];

					final IntegerRange integerRange = new IntegerRange(
							(Integer)range.getLower(), range.includeLower(),
							(Integer)range.getUpper(), range.includeUpper());
					
					integerRanges[i] = integerRange;
				}
				criterium = new IntegerRangesCriterium(attribute, integerRanges);
				break;
				
			case DECIMAL:
				final DecimalRange [] decimalRanges = new DecimalRange[ranges.length];

				for (int i = 0; i < ranges.length; ++ i) {
					final SearchRange range = ranges[i];

					final DecimalRange decimalRange = new DecimalRange(
							(BigDecimal)range.getLower(), range.includeLower(),
							(BigDecimal)range.getUpper(), range.includeUpper());
					
					decimalRanges[i] = decimalRange;
				}

				criterium = new DecimalRangesCriterium(attribute, decimalRanges);
				break;
				
			default:
				throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
			}
		}
		else if (searchCriterium.getValues() != null) {
			
			final boolean includeItemsWithNoValue = searchCriterium.getOtherSelected() != null
					? searchCriterium.getOtherSelected()
				: false;
			
			switch (attribute.getAttributeType()) {
			case STRING:
				criterium = new StringInCriterium(attribute, convertArray(searchCriterium.getValues(), length -> new String[length], o -> (String)o.getValue()), includeItemsWithNoValue);
				break;

			case INTEGER:
				criterium = new IntegerInCriterium(attribute, convertArray(searchCriterium.getValues(), length -> new Integer[length], o -> (Integer)o.getValue()), includeItemsWithNoValue);
				break;

			case DECIMAL:
				criterium = new DecimalInCriterium(attribute, convertArray(searchCriterium.getValues(), length -> new BigDecimal[length], o -> (BigDecimal)o.getValue()), includeItemsWithNoValue);
				break;
				
			case ENUM:
				// Find enum-class from attribute
				criterium = makeEnumCriterium(attribute, searchCriterium, includeItemsWithNoValue);
				break;

			default:
				throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
			}
		}
		else {
			throw new IllegalArgumentException("Neither values nor ranges set");
		}

		return criterium;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static EnumInCriterium<?> makeEnumCriterium(ItemAttribute attribute, SearchCriterium searchCriterium, boolean includeItemsWithNoValue) {
		final Class enumClass = attribute.getAttributeValueClass();

		return new EnumInCriterium<>(
				attribute,
				(Enum[])convertArray(searchCriterium.getValues(), length -> new Enum[length], o -> (Enum)Enum.valueOf(enumClass, (String)o.getValue())),
				includeItemsWithNoValue);
	}
	
	private static <T, R> R [] convertArray(T [] input, Function<Integer, R []> createArray, Function<T, R> convert) {
		final R []output = createArray.apply(input.length);
		
		for (int i = 0; i < input.length; ++ i) {
			output[i] = convert.apply(input[i]);
		}

		return output;
	}
	
	private static SearchFacetsResult convertFacets(ItemsFacets facets) {
		final SearchFacetsResult result = new SearchFacetsResult();
		
		final List<SearchFacetedTypeResult> typeFacetsResult = new ArrayList<>(facets.getTypes().size());
		
		for (TypeFacets typeFacet : facets.getTypes()) {
			final SearchFacetedTypeResult typeResult = new SearchFacetedTypeResult();

			final List<SearchFacetedAttributeResult> facetAttributesResult = convertAttributeList(typeFacet.getAttributes());
			
			typeResult.setType(getTypeId(typeFacet.getType()));
			typeResult.setDisplayName(getTypeDisplayName(typeFacet.getType()));
			typeResult.setAttributes(facetAttributesResult);
			
			typeFacetsResult.add(typeResult);
		}
		
		result.setTypes(typeFacetsResult);

		return result;
	}
	
	private static List<SearchFacetedAttributeResult> convertAttributeList(List<IndexFacetedAttributeResult> attributes) {

		final List<SearchFacetedAttributeResult> facetAttributesResult = new ArrayList<>(attributes.size());

		for (IndexFacetedAttributeResult indexFacetedAttribute : attributes) {
			final SearchFacetedAttributeResult searchFacetedAttribute;

			if (indexFacetedAttribute instanceof IndexSingleValueFacetedAttributeResult) {
				final IndexSingleValueFacetedAttributeResult indexSingleValueFacetedAttributeResult
						= (IndexSingleValueFacetedAttributeResult)indexFacetedAttribute;
				
				final SearchSingleValueFacetedAttributeResult searchSingleValueFacetedAttribute = new SearchSingleValueFacetedAttributeResult();
				
				final List<SearchSingleValueFacet> searchValues = new ArrayList<>(indexSingleValueFacetedAttributeResult.getValues().size());
				for (IndexSingleValueFacet indexValue : indexSingleValueFacetedAttributeResult.getValues()) {
					
					final SearchSingleValueFacet searchValue = new SearchSingleValueFacet();
					
					searchValue.setValue(indexValue.getValue());
					searchValue.setDisplayValue(indexValue.getDisplayValue());
					searchValue.setMatchCount(indexValue.getMatchCount());
					
					if (indexValue.getSubFacets() != null) {
						searchValue.setSubAttributes(convertAttributeList(indexValue.getSubFacets()));
					}
					
					searchValues.add(searchValue);
				}
				
				searchSingleValueFacetedAttribute.setValues(searchValues);
				if (indexSingleValueFacetedAttributeResult.getNoAttributeValueCount() != 0) {
					searchSingleValueFacetedAttribute.setNoAttributeValueCount(indexSingleValueFacetedAttributeResult.getNoAttributeValueCount());
				}

				searchFacetedAttribute = searchSingleValueFacetedAttribute;
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
			searchFacetedAttribute.setName(indexFacetedAttribute.getAttribute().getFacetDisplayName());
			
			facetAttributesResult.add(searchFacetedAttribute);
		}

		return facetAttributesResult;
	}
	
	public byte[] searchReturnCompressed(String freeText, String [] types, SearchCriterium [] criteria, Integer pageNo, Integer itemsPerPage, HttpServletRequest request) {
		// Return result as a compressed array (non JSON) of
		// - IDs, in order
		// titles, in order
		// thumbnail sizes (byte width, byte height), in order
		
		final SearchResult searchResult = this.search(freeText, types, criteria, pageNo, itemsPerPage, false, request);

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
		

	@Path("/thumbnails")
	// Get item thumbnails as one big compressed JPEG? Or as a stream of JPEGs?
	public byte [] getThumbnails(String [] itemIds, HttpServletRequest request) {

		// Compact item IDs
		final List<String> filtered = Arrays.stream(itemIds)
			.filter(id -> id != null)
			.map(id -> id.trim())
			.filter(id -> !id.isEmpty())
			.collect(Collectors.toList());
		
		itemIds = filtered.toArray(new String[filtered.size()]);

		// Return thumbnails as concatenated JPEGs
		InputStream inputStream = null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(50000);

		try {
			inputStream = getItemDAO(request).retrieveAndConcatenateThumbnails(itemIds);

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
	
	private SearchResult makeTestResult() {
		final SearchResult result = new SearchResult();
		
		final SearchFacetsResult facets = new SearchFacetsResult();
		
		final SearchFacetedTypeResult sports = new SearchFacetedTypeResult();
		
		sports.setType("sports");
		sports.setDisplayName("Sports");
		
		final SearchFacetedTypeResult snowboard = new SearchFacetedTypeResult();
		
		snowboard.setType("snowboard");
		snowboard.setDisplayName("Snowboards");
		
		sports.setSubTypes(Arrays.asList(snowboard));

		final SearchSingleValueFacetedAttributeResult jonesModelAttribute = new SearchSingleValueFacetedAttributeResult();
		jonesModelAttribute.setId("model");
		jonesModelAttribute.setName("Model");
		jonesModelAttribute.setValues(Arrays.asList(
				new SearchSingleValueFacet("1234", 2),
				new SearchSingleValueFacet("5678", 1))
		);

		final SearchSingleValueFacetedAttributeResult burtonModelAttribute = new SearchSingleValueFacetedAttributeResult();
		burtonModelAttribute.setId("model");
		burtonModelAttribute.setName("Model");
		burtonModelAttribute.setValues(Arrays.asList(
				new SearchSingleValueFacet("8765", 3),
				new SearchSingleValueFacet("4321", 2))
		);

		final SearchSingleValueFacetedAttributeResult makeAttribute = new SearchSingleValueFacetedAttributeResult();
		makeAttribute.setId("make");
		makeAttribute.setName("Make");
		makeAttribute.setValues(Arrays.asList(
				new SearchSingleValueFacet("Jones", 3, jonesModelAttribute),
				new SearchSingleValueFacet("Burton", 5, burtonModelAttribute))
		);


		final SearchRangeFacetedAttributeResult lengthAttribute = new SearchRangeFacetedAttributeResult();
		
		lengthAttribute.setId("length");
		lengthAttribute.setName("Length");
		lengthAttribute.setRanges(Arrays.asList(
				new SearchFacetedAttributeDecimalRangeResult(null, new BigDecimal("160.0"), 2),
				new SearchFacetedAttributeDecimalRangeResult(new BigDecimal("160.0"), new BigDecimal("165.0"), 2),
				new SearchFacetedAttributeDecimalRangeResult(new BigDecimal("165.0"), new BigDecimal("170.0"), 3),
				new SearchFacetedAttributeDecimalRangeResult(new BigDecimal("170.0"), null, 1))
		);

		snowboard.setAttributes(Arrays.asList(makeAttribute, lengthAttribute));
		
		final SearchFacetedTypeResult housing = new SearchFacetedTypeResult();
		
		housing.setType("housing");
		housing.setDisplayName("Housing");

		final SearchFacetedTypeResult apartments = new SearchFacetedTypeResult();

		apartments.setType("apartment");
		apartments.setDisplayName("Apartments");
		
		housing.setSubTypes(Arrays.asList(apartments));
		
		facets.setTypes(Arrays.asList(sports, housing));
		
		result.setFacets(facets);
		
		return result;
	}
}
