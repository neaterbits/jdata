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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.test.cv.common.IOUtil;
import com.test.cv.common.StringUtil;
import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.dao.SearchException;
import com.test.cv.dao.index.IndexSearchDAO;
import com.test.cv.integrationtest.IntegrationTestHelper;
import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.SortAttribute;
import com.test.cv.model.SortAttributeAndOrder;
import com.test.cv.model.SortOrder;
import com.test.cv.model.annotations.SortableType;
import com.test.cv.model.attributes.AttributeType;
import com.test.cv.model.attributes.ClassAttributes;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.model.items.TypeInfo;
import com.test.cv.search.SearchItem;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.criteria.DecimalInCriterium;
import com.test.cv.search.criteria.DecimalRange;
import com.test.cv.search.criteria.DecimalRangesCriterium;
import com.test.cv.search.criteria.EnumInCriterium;
import com.test.cv.search.criteria.InCriteriumValue;
import com.test.cv.search.criteria.IntegerInCriterium;
import com.test.cv.search.criteria.IntegerRange;
import com.test.cv.search.criteria.IntegerRangesCriterium;
import com.test.cv.search.criteria.NoValueCriterium;
import com.test.cv.search.criteria.StringInCriterium;
import com.test.cv.search.facets.IndexFacetedAttributeResult;
import com.test.cv.search.facets.IndexRangeFacetedAttributeResult;
import com.test.cv.search.facets.IndexSingleValueFacet;
import com.test.cv.search.facets.IndexSingleValueFacetedAttributeResult;
import com.test.cv.search.facets.ItemsFacets;
import com.test.cv.search.facets.TypeFacets;


@Path("/search")
public class SearchService extends BaseService {
	
	private static final String ASCENDING = "ascending";
	private static final String DESCENDING = "descending";
	
	public SearchService(String localFileDir) {
		super(localFileDir);
	}

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
	public SearchResult search(String [] types, String freeText, SearchCriterium [] criteria, String [] sortOrder, String [] fields, Integer pageNo, Integer itemsPerPage, Boolean testdata, HttpServletRequest request) {

		
		if (types == null) {
			// No types selected, ought to return empty resultset
			types = new String[0];  
		}
		else if (types.length == 1 && types[0].equals("_all_")) {
			// Hack to get all types at the start and separate this case from the "no types" case above
			types = ItemTypes.getTypeNames();
		}
		
		
		final List<Class<? extends Item>> typesList = new ArrayList<>(types.length);
		
		for (String typeName : types) {
			final TypeInfo typeInfo = ItemTypes.getTypeByName(typeName);
			
			if (typeInfo == null) {
				throw new IllegalArgumentException("Unknown type " + typeName);
			}

			typesList.add(typeInfo.getType());
		}

		final Set<Class<? extends Item>> baseTypes = ItemTypes.getBaseTypes(typesList);
		
		final List<SortAttributeAndOrder> sortAttributes;
		if (sortOrder != null) {

			final Set<Class<? extends Item>> sortOrderTypes = new HashSet<>(baseTypes.size() + typesList.size());

			// Must add base types to sortorder types since some attributes are only in base types
			sortOrderTypes.addAll(baseTypes);
			sortOrderTypes.addAll(typesList);
			
			sortAttributes = decodeSortOrders(sortOrderTypes, sortOrder);
		}
		else {
			// Get sort order from common denominator among types
			sortAttributes = computeAndSortPossibleSortAttributes(typesList)
					.stream().map(a -> new SortAttributeAndOrder(a, SortOrder.ASCENDING))
					.collect(Collectors.toList());
		}
		
		final List<ItemAttribute> responseFieldAttributes;
		if (fields != null && fields.length > 0) {
			responseFieldAttributes = new ArrayList<>();

			for (String field : fields) {
				for (Class<? extends Item> baseType : baseTypes) {
					final TypeInfo typeInfo = ItemTypes.getTypeInfo(baseType);
					
					final ItemAttribute attribute = typeInfo.getAttributes().getByName(field);
					
					if (attribute != null) {
						responseFieldAttributes.add(attribute);
						break;
					}
				}
			}
		}
		else {
			responseFieldAttributes = Collections.emptyList();
		}

		final SearchResult result;
		if (testdata != null && testdata && isTest()) {
			// Return a hardcoded testresult for simple local testing
			result = makeTestResult();
		}
		else {
			result = searchInDB(typesList, freeText, criteria, sortAttributes, responseFieldAttributes, pageNo, itemsPerPage, request);
		}

		return result;
	}

	private SearchResult searchInDB(
			List<Class<? extends Item>> types,
			String freeText,
			SearchCriterium [] criteria,
			List<SortAttributeAndOrder> sortAttributes,
			List<ItemAttribute> responseFieldAttributes,
			Integer pageNo, Integer itemsPerPage,
			HttpServletRequest request) {
		

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
				final Set<ItemAttribute> responseFieldSet = (responseFieldAttributes != null && !responseFieldAttributes.isEmpty())
						? new HashSet<>(responseFieldAttributes)
						: Collections.emptySet();

				cursor = searchDAO.search(types, freeText, daoCriteria, sortAttributes, responseFieldSet, ItemTypes.getFacetAttributes(types));
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

			final Set<Class<? extends Item>> sortOrderTypes = new HashSet<>(types);

			if (cursor.getFacets() != null) {
				
				// If facets, only add types from facet result
				final List<Class<? extends Item>> facetTypes = cursor.getFacets().getTypes()
						.stream()
						.filter(facetType -> !facetType.getAttributes().isEmpty())
						.map(facetType -> facetType.getType())
						.collect(Collectors.toList());
				
				sortOrderTypes.retainAll(facetTypes);
				
				result.setFacets(convertFacets(cursor.getFacets()));
			}
			
			result.setSortOrders(computeAndSortPossibleSortOrders(sortOrderTypes));

			for (int i = 0; i < numFound; ++ i) {
				final SearchItem foundItem = found.get(i);
				final Object [] fieldValues = new Object[responseFieldAttributes.size()];

				for (int fieldNo = 0; fieldNo < responseFieldAttributes.size(); ++ fieldNo) {
					final ItemAttribute attribute = responseFieldAttributes.get(i);

					fieldValues[fieldNo] = foundItem.getAttributeValue(attribute);
				}

				items[i] = new SearchItemResult(
						foundItem.getItemId(),
						foundItem.getTitle(),
						foundItem.getThumbWidth() != null ? foundItem.getThumbWidth() : THUMBNAIL_MAX_SIZE,
						foundItem.getThumbHeight() != null ? foundItem.getThumbHeight() : THUMBNAIL_MAX_SIZE,
						fieldValues);
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


	private static List<ItemAttribute> getSortableAttributesFromType(Class<? extends Item> type) {
		
		final ClassAttributes attrs = ItemTypes.getTypeInfo(type).getAttributes();

		final List<ItemAttribute> sortAttributes = new ArrayList<>();

		attrs.forEach(attr -> {
			if (attr.isSortable()) {
				sortAttributes.add(attr);
			}
				
		});
		
		return sortAttributes;
	}

	
	private static SearchSortOrderAlternative [] getSortOrdersFromAttributes(List<SortAttribute> attributes) {
		
		final List<SearchSortOrderAlternative> order = new ArrayList<>();

		attributes.forEach(attr -> {
			final SortableType sortableType = attr.getSortableType();
			
			final String sortOrderName = attr.encodeToString();
			final String sortOrderDisplayName = attr.getSortableTitle();
			
			if (sortableType == SortableType.NUMERICAL || sortableType == SortableType.TIME) {
				order.add(new SearchSortOrderAlternative(sortOrderName + '_' + ASCENDING, sortOrderDisplayName + " - low to high"));
				order.add(new SearchSortOrderAlternative(sortOrderName + '_' + DESCENDING, sortOrderDisplayName + " - high to low"));
			}
			else {
				order.add(new SearchSortOrderAlternative(sortOrderName, sortOrderDisplayName));
			}
		});
		
		final SearchSortOrderAlternative [] array;
		
		if (order.isEmpty()) {
			array = new SearchSortOrderAlternative[0];
		}
		else {
			array = order.toArray(new SearchSortOrderAlternative[order.size()]);
		}

		return array;
	}

	// Need to hash on declaring-class for attribute
	// so that base class attributes are only counted once no matter the subclass (eg 'Title' is in baseclass
	// for both Car and Snowboard)
	private static List<SortAttribute> computeAndSortPossibleSortAttributes(Collection<Class<? extends Item>> types) {

		final List<SortAttribute> attributes;
		
		if (types.isEmpty()) {
			attributes = Collections.emptyList();
		}
		else if (types.size() == 1) {
			 attributes = getSortableAttributesFromType(types.iterator().next()).stream()
					 .map(a -> a.makeSortAttribute())
					 .collect(Collectors.toList());
		}
		else {
		
			// Only return those that are common to all types?
			final Set<SortAttribute> commonSortableAttributes = new HashSet<>();

			for (Class<? extends Item> type : types) {
				final List<SortAttribute> typeAttributes = getSortableAttributesFromType(type).stream()
						.map(a -> a.makeSortAttribute())
						.collect(Collectors.toList());
				
				if (commonSortableAttributes.isEmpty()) {
					commonSortableAttributes.addAll(typeAttributes);
				}
				else {
					commonSortableAttributes.retainAll(typeAttributes);
				}
			}

			attributes = new ArrayList<>(commonSortableAttributes);
		}

		if (attributes.size() > 1) {
			Collections.sort(attributes, SortAttribute.SORTABLE_PRIORITY_COMPARATOR);
		}
		
		return attributes;
	}

	private static SearchSortOrderAlternative [] computeAndSortPossibleSortOrders(Collection<Class<? extends Item>> types) {
	
		final List<SortAttribute> attributes = computeAndSortPossibleSortAttributes(types);

		// Convert to sort orders
		return getSortOrdersFromAttributes(attributes);
	}
	
	private static List<SortAttributeAndOrder> decodeSortOrders(Collection<Class<? extends Item>> types, String [] sortOrders) {
		
		final List<SortAttributeAndOrder> result = new ArrayList<>(sortOrders.length);
		
		for (String sortOrder : sortOrders) {
			final String [] parts = StringUtil.split(sortOrder, '_');

			final SortAttributeAndOrder attributeAndOrder;
			
			if (parts.length == 1) {
				// Only classname:attrname
				final SortAttribute attribute = SortAttribute.decode(types, parts[0]);
			
				attributeAndOrder = new SortAttributeAndOrder(attribute, SortOrder.ASCENDING);
			}
			else if (parts.length == 2) {
				// sort order specified as well
				final SortAttribute attribute = SortAttribute.decode(types, parts[0]);
				
				final SortOrder sortOrderEnum;
				
				switch (parts[1]) {
				case ASCENDING:
					sortOrderEnum = SortOrder.ASCENDING;
					break;
					
				case DESCENDING:
					sortOrderEnum = SortOrder.DESCENDING;
					break;
					
				default:
					throw new IllegalArgumentException("Unknown sort order " + parts[1]);
				}

				attributeAndOrder = new SortAttributeAndOrder(attribute, sortOrderEnum);
			}
			else {
				throw new IllegalArgumentException("Unable to parse sort order " + sortOrder);
			}
			
			result.add(attributeAndOrder);
		}

		return result;
	}
	
	private static List<Criterium> convertCriteria(SearchCriterium [] searchCriteria) {
		final List<Criterium> criteria = new ArrayList<>(searchCriteria.length);
		
		for (int i = 0; i < searchCriteria.length; ++ i) {
			final Criterium criterium = convertCriterium(searchCriteria[i]);
			
			if (criterium == null) {
				// Probably no checkboxes were selected
			}
			else {
				criteria.add(criterium);
			}
		}

		return criteria;
	}
	
	private static BigDecimal toDecimal(Object rangeNo) {
		final BigDecimal result;
		
		if (rangeNo == null) {
			result = null;
		}
		else if (rangeNo instanceof Integer) {
			result = BigDecimal.valueOf((Integer)rangeNo);
		}
		else if (rangeNo instanceof Double) {
			result = BigDecimal.valueOf((Double)rangeNo);
		}
		else if (rangeNo instanceof BigDecimal) {
			result = (BigDecimal)rangeNo;
		}
		else {
			throw new IllegalArgumentException("Unknown rangeNo type " + rangeNo.getClass());
		}
		
		return result;
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

		final boolean includeItemsWithNoValue = searchCriterium.getOtherSelected() != null
				? searchCriterium.getOtherSelected()
			: false;
		
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
				criterium = new IntegerRangesCriterium(attribute, integerRanges, includeItemsWithNoValue);
				break;
				
			case DECIMAL:
				final DecimalRange [] decimalRanges = new DecimalRange[ranges.length];

				for (int i = 0; i < ranges.length; ++ i) {
					final SearchRange range = ranges[i];

					final DecimalRange decimalRange = new DecimalRange(
							toDecimal(range.getLower()), range.includeLower(),
							toDecimal(range.getUpper()), range.includeUpper());
					
					decimalRanges[i] = decimalRange;
				}

				criterium = new DecimalRangesCriterium(attribute, decimalRanges, includeItemsWithNoValue);
				break;
				
			default:
				throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
			}
		}
		else if (searchCriterium.getValues() != null) {
			
			
			switch (attribute.getAttributeType()) {
			case STRING:
				criterium = new StringInCriterium(attribute, convertCriteriaValues(searchCriterium, o -> (String)o), includeItemsWithNoValue);
				break;

			case INTEGER:
				criterium = new IntegerInCriterium(attribute, convertCriteriaValues(searchCriterium, o -> (Integer)o), includeItemsWithNoValue);
				break;

			case DECIMAL:
				criterium = new DecimalInCriterium(attribute, convertCriteriaValues(searchCriterium, o -> toDecimal(o)), includeItemsWithNoValue);
				break;
				
			case ENUM:
				// Find enum-class from attribute
				criterium = makeEnumCriterium(attribute, searchCriterium, includeItemsWithNoValue);
				break;

			default:
				throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
			}
		}
		else if (searchCriterium.getOtherSelected() != null && searchCriterium.getOtherSelected()) {
			// Only other-values, eg "other" selected
			criterium = new NoValueCriterium(attribute);
		}
		else {
			// Nothing selected for criterium so ignore
			criterium = null;
		}

		return criterium;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T extends Comparable<T>> List<InCriteriumValue<T>> convertCriteriaValues(SearchCriterium sc, Function<Object, T> convertValue) {
		
		final SearchCriteriumValue [] values = sc.getValues();
		
		final List<InCriteriumValue<T>> list = new ArrayList<>(values.length);
		
		for (int i = 0; i < values.length; ++ i) {
			final SearchCriteriumValue value = values[i];
			final SearchCriterium [] subCriteria = value.getSubCriteria();
			final List<Criterium> sub;

			if (subCriteria != null) {
				// Convert subcriteria as well
				sub = (List)convertCriteria(subCriteria);
			}
			else {
				sub = null;
			}
			
			final T converted = convertValue.apply(value.getValue());

			list.add(new InCriteriumValue<T>(converted, sub));
		}

		return list;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <E extends Enum<E>> EnumInCriterium<E> makeEnumCriterium(ItemAttribute attribute, SearchCriterium searchCriterium, boolean includeItemsWithNoValue) {
		final Class enumClass = attribute.getAttributeValueClass();

		return new EnumInCriterium<E>(
				attribute,
				convertCriteriaValues(searchCriterium, o -> (E)Enum.valueOf(enumClass, (String)o)),
				includeItemsWithNoValue);
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

			if (indexFacetedAttribute.getNoAttributeValueCount() != 0) {
				searchFacetedAttribute.setNoAttributeValueCount(indexFacetedAttribute.getNoAttributeValueCount());
			}

			searchFacetedAttribute.setId(indexFacetedAttribute.getAttribute().getName());
			searchFacetedAttribute.setName(indexFacetedAttribute.getAttribute().getFacetDisplayName());
			
			facetAttributesResult.add(searchFacetedAttribute);
		}

		return facetAttributesResult;
	}
	
	public byte[] searchReturnCompressed(String freeText, String [] types, SearchCriterium [] criteria, String[] sortOrder, String [] fields, Integer pageNo, Integer itemsPerPage, HttpServletRequest request) {

		// Return result as a compressed array (non JSON) of
		// - IDs, in order
		// titles, in order
		// thumbnail sizes (byte width, byte height), in order
		// fields, in orders

		final SearchResult searchResult = this.search(
				types,
				freeText, criteria, sortOrder,
				fields,
				pageNo, itemsPerPage,
				false,
				request);

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
