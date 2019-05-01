package com.test.salesportal.index.elasticsearch;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.Range.Bucket;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import com.test.salesportal.common.ItemId;
import com.test.salesportal.index.IndexSearchCursor;
import com.test.salesportal.index.IndexSearchItem;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.index.ItemIndexException;
import com.test.salesportal.index.MatchNoneIndexSearchCursor;
import com.test.salesportal.model.DistinctAttribute;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.ItemAttributeValue;
import com.test.salesportal.model.SortAttributeAndOrder;
import com.test.salesportal.model.attributes.AttributeType;
import com.test.salesportal.model.attributes.facets.FacetedAttributeDecimalRange;
import com.test.salesportal.model.attributes.facets.FacetedAttributeIntegerRange;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.search.AttributeValues;
import com.test.salesportal.search.FieldValues;
import com.test.salesportal.search.SearchItem;
import com.test.salesportal.search.criteria.ComparisonCriterium;
import com.test.salesportal.search.criteria.ComparisonOperator;
import com.test.salesportal.search.criteria.Criterium;
import com.test.salesportal.search.criteria.DecimalCriterium;
import com.test.salesportal.search.criteria.InCriterium;
import com.test.salesportal.search.criteria.InCriteriumValue;
import com.test.salesportal.search.criteria.IntegerCriterium;
import com.test.salesportal.search.criteria.NoValueCriterium;
import com.test.salesportal.search.criteria.StringCriterium;
import com.test.salesportal.search.facets.FacetUtils;
import com.test.salesportal.search.facets.IndexFacetedAttributeResult;
import com.test.salesportal.search.facets.IndexRangeFacetedAttributeResult;
import com.test.salesportal.search.facets.IndexSingleValueFacet;
import com.test.salesportal.search.facets.IndexSingleValueFacetedAttributeResult;
import com.test.salesportal.search.facets.ItemsFacets;
import com.test.salesportal.search.facets.TypeFacets;

// TODO use multiple indices, one per type? instead of merging all into one index. Multiple types per index is obsleted in >= 6.x
// See https://www.elastic.co/guide/en/elasticsearch/reference/5.3/general-recommendations.html#sparsity


public class ElasticSearchIndex implements ItemIndex {

	private final RestHighLevelClient client;
	

	private static final String INDEX_NAME = "items";

	private static final String FIELD_USER_ID = "userId";
	private static final String FIELD_THUMBS = "thumbs";

	private static final String MISSING_PREFIX = "missing-";
	
	// Name of field to represent title
	private final ItemAttribute titleAttribute;
	
	private static final ESTypeHandling TYPE_HANDLING = new ESTypeHandlingOneTypePerIndexCustomTypeField();
	
	public ElasticSearchIndex(String endpointUrl, ItemAttribute titleAttribute) throws ItemIndexException {

		if (titleAttribute == null) {
			throw new IllegalArgumentException("titleAttribute == null");
		}

		this.titleAttribute = titleAttribute;

		this.client = new RestHighLevelClient(RestClient.builder(new HttpHost(endpointUrl)));
		
		createIndexIfNotExists();
	}
	
	@Override
	public void close() throws Exception {
		client.close();
	}

	@Override
	public void indexItemAttributes(String userId, Class<? extends Item> itemType, String typeName,
			List<ItemAttributeValue<?>> attributeValues) throws ItemIndexException {

		final String type = getTypeName(itemType);
		
		String id = null;
		
		for (ItemAttributeValue<?> value : attributeValues) {
			if (isIdAttribute(value.getAttribute())) {
				id = (String)value.getValue();
				break;
			}
		}
		
		if (id == null) {
			throw new IllegalArgumentException("No id found for item of type " + itemType);
		}
		
		final IndexRequest request = new IndexRequest(INDEX_NAME, type, id);

		final StringBuilder sb = new StringBuilder("{\n");

		sb.append("  ").append('"').append(FIELD_USER_ID).append('"').append(" : ").append('"').append(userId).append('"');
		
		final Map<String, Object> customFields = TYPE_HANDLING.indexCustomFields(itemType);
		
		if (customFields != null) {
			for (Map.Entry<String, Object> entry : customFields.entrySet()) {
				sb.append(",\n");
				
				sb.append("  ").append('"').append(entry.getKey()).append('"').append(" : ");

				final Object v = entry.getValue();
				
				if (v instanceof Enum<?>) {
					sb.append('"').append(((Enum<?>)v).name()).append('"');
				}
				else if (v instanceof String) {
					sb.append('"').append((String)v).append('"');
				}
				else if (v instanceof Integer || v instanceof Long) {
					sb.append(v.toString());
				}
				else {
					throw new UnsupportedOperationException("Unknown value: " + v.getClass());
				}
			}
		}

		for (ItemAttributeValue<?> value : attributeValues) {

			sb.append(",\n");

			final ItemAttribute attribute = value.getAttribute();
			final Object v = value.getValue();
			
			// Index freetext as extra field
			if (attribute.isFreetext()) {
				// Index freetext field as well
				final String freetextFieldName = ItemIndex.freetextFieldName(attribute);

				sb.append("  ").append('"').append(freetextFieldName).append('"').append(" : ").append('"').append((String)v).append('"').append(",\n");
			}

			sb.append("  ").append('"').append(ItemIndex.fieldName(attribute)).append('"').append(" : ");

			final String s;
			
			switch (attribute.getAttributeType()) {
			case STRING:
				s = '"' + (String)v + '"';
				break;
				
			case INTEGER:
				s = String.valueOf((Integer)v);
				break;

			case LONG:
				s = String.valueOf((Long)v);
				break;

			case DECIMAL:
				s = ((BigDecimal)v).toPlainString();
				break;
				
			case DATE:
				s = String.valueOf(((Date)v).getTime()); // Set in schema type already
				break;
				
			case ENUM:
				s = '"' + String.valueOf(((Enum<?>)v).name()) + '"';
				break;
				
			case BOOLEAN:
				s = ((Boolean)v) ? "true" : "false";
				break;
				
			default:
				throw new UnsupportedOperationException("Unknown attribute type: " + attribute.getAttributeType());
			}

			sb.append(s);
		}		

		sb.append("}");
		
		request.source(sb.toString(), XContentType.JSON);
		
		try {
			client.index(request);
		} catch (IOException ex) {
			throw new ItemIndexException("Failed to index", ex);
		}
	}
	
	private static QueryBuilder createFreetextQuery(Collection<Class<? extends Item>> types, String freeText) {
		
		// Get SortAttribute since this has equals() and hashCode() on base class,
		// this makes us find distinct attributes
		final Set<DistinctAttribute> distinctAttributes = ItemTypes.getFreetextAttributes(types);
		
		final QueryBuilder query;
		
		if (distinctAttributes.isEmpty()) {
			query = null;
		}
		else if (distinctAttributes.size() == 1) {
			final String fieldName = ItemIndex.freetextFieldName(distinctAttributes.iterator().next());

			query = QueryBuilders.termQuery(fieldName, freeText);
		}
		else {
		
			final BoolQueryBuilder booleanQuery = QueryBuilders.boolQuery();
		
			for (DistinctAttribute attribute : distinctAttributes) {
				final String fieldName = ItemIndex.freetextFieldName(attribute);
				
				final QueryBuilder termQuery = QueryBuilders.termQuery(fieldName, freeText);
				
				booleanQuery.should(termQuery);
			}
			
			query = booleanQuery;
		}
		
		return query;
	}

	
	@Override
	public void deleteItem(String itemId, Class<? extends Item> type) throws ItemIndexException {
		
		final DeleteRequest request = new DeleteRequest(INDEX_NAME, getTypeName(type), itemId);

		try {
			client.delete(request);
		} catch (IOException ex) {
			throw new ItemIndexException("Exception while deleting", ex);
		}
	}

	@Override
	public void indexThumbnailSize(String itemId, Class<? extends Item> type, int photoNo, int thumbWidth, int thumbHeight)
			throws ItemIndexException {
		
		final ThumbSizes thumbSizes = getThumbs(itemId, type);
		
		final Integer [] sizes = updateThumbnailSizeArray(thumbSizes.sizes, photoNo, thumbWidth, thumbHeight, 0,
				f -> f,
				length -> new Integer[length],
				(tw, th) -> tw << 16 | th);
		
		// Update
		updateThumbs(itemId, getTypeName(type), sizes);
	}
	
	private static class ThumbSizes {
		private final String itemType;
		private final Integer [] sizes;

		public ThumbSizes(String itemType, Integer[] sizes) {
			if (itemType == null) {
				throw new IllegalArgumentException("itemType == null");
			}

 			this.itemType = itemType;
			this.sizes = sizes;
		}
	}

	private ThumbSizes getThumbs(String itemId, Class<? extends Item> itemType) throws ItemIndexException {
		return getThumbs(itemId, getTypeName(itemType));
	}
	
	private ThumbSizes getThumbs(String itemId, String itemType) throws ItemIndexException {
		final GetRequest request = new GetRequest(INDEX_NAME, itemType, itemId);
		
		final FetchSourceContext sourceContext = new FetchSourceContext(true, new String[] { FIELD_THUMBS }, null);
		
		request.fetchSourceContext(sourceContext);
		
		final GetResponse response;
		try {
			response = client.get(request);
		} catch (IOException ex) {
			throw new ItemIndexException("Failed to get item with ID " + itemId, ex);
		}
		
		// Encoded width/height as integers
		
		final Map<String, Object> source = response.getSourceAsMap();

		@SuppressWarnings("unchecked")
		final ArrayList<Integer> thumbSizes = (ArrayList<Integer>) source.get(FIELD_THUMBS);
		
		final Integer [] integers;
		
		if (thumbSizes != null) {
			integers = new Integer[thumbSizes.size()];
			for (int i = 0; i < thumbSizes.size(); ++ i) {
				integers[i] = thumbSizes.get(i);
			}
		}
		else {
			integers = null;
		}

		return new ThumbSizes(response.getType(), integers);
	}

	private void updateThumbs(String itemId, String type, Integer [] sizes) throws ItemIndexException {
		final UpdateRequest update = new UpdateRequest(INDEX_NAME, type, itemId);
		
		final StringBuilder sb = new StringBuilder();
		
		sb.append("{ ").append('"').append(FIELD_THUMBS).append('"').append(" : ");
		
		sb.append("[ ");
		
		for (int i = 0; i < sizes.length; ++ i) {
			
			if (i > 0) {
				sb.append(", ");
			}

			sb.append(sizes[i]);
		}
		
		sb.append(" ]");
		sb.append(" }");
		
		update.doc(sb.toString(), XContentType.JSON);
		
		try {
			client.update(update);
		} catch (IOException ex) {
			throw new ItemIndexException("Exception during update of thumbs", ex);
		}
	}
	
	@Override
	public void deletePhotoAndThumbnailForItem(String itemId, Class<? extends Item> type, int photoNo) throws ItemIndexException {
		final ThumbSizes existing = getThumbs(itemId, type);
		
		final Integer [] updated = deleteThumbnail(existing.sizes, photoNo, length -> new Integer[length]);

		updateThumbs(itemId, existing.itemType, updated);
	}

	@Override
	public void movePhotoAndThumbnailForItem(String itemId, Class<? extends Item> type, int photoNo, int toIndex) throws ItemIndexException {
		final ThumbSizes existing = getThumbs(itemId, type);
		
		final Integer[] moved = moveThumbnail(existing.sizes, existing.sizes[photoNo], photoNo, toIndex, length -> new Integer[length]);
		
		updateThumbs(itemId, ItemTypes.getTypeName(type), moved);
	}

	@Override
	public ItemId[] expandToItemIdUserId(String[] itemIds) throws ItemIndexException {

		final SearchRequest request = new SearchRequest(INDEX_NAME);
		
		final Map<String, Integer> idToIndex = new HashMap<>();
		
		for (int i = 0; i < itemIds.length; ++ i) {
			idToIndex.put(itemIds[i], i);
		}
 		
		final SearchSourceBuilder sourceBuilder = request.source();
		
		sourceBuilder.fetchSource(new String [] { FIELD_USER_ID }, null);

		sourceBuilder.query(QueryBuilders.idsQuery().addIds(itemIds)).fetchSource();

		SearchResponse response;
		try {
			response = client.search(request);
		} catch (IOException ex) {
			throw new ItemIndexException("Exception while searching", ex);
		}
		
		final SearchHit [] hits = response.getHits().getHits();
		
		if (hits.length != itemIds.length) {
			throw new IllegalStateException("Could not find all item IDs");
		}
		
		final ItemId [] result = new ItemId[hits.length];
		
		for (SearchHit hit : hits) {
			final String id = hit.getId();
			final String userId = (String)hit.getSourceAsMap().get(FIELD_USER_ID);
			
			final int index = idToIndex.get(id);
			
			if (result[index] != null) {
				throw new IllegalStateException("Multple entries at " +  index + " for id " + id);
			}
			
			result[index] = new ItemId(userId, id);
		}

		return result;
	}

	@Override
	public IndexSearchCursor search(
			List<Class<? extends Item>> types,
			String freeText, List<Criterium> criteria,
			List<SortAttributeAndOrder> sortOrder,
			Set<ItemAttribute> fieldAttributes,
			Set<ItemAttribute> facetAttributes)
			throws ItemIndexException {

		final IndexSearchCursor cursor;

		if (types.isEmpty()) {
			cursor = new MatchNoneIndexSearchCursor();
		}
		else {
			// Must add both query and also aggregations if necessary
			final SearchRequest request = new SearchRequest(INDEX_NAME);

			final SearchSourceBuilder sourceBuilder = request.source();
			final QueryBuilder queryBuilder;
			final QueryBuilder criteriaBuilder;

			if (criteria == null) {
				criteriaBuilder = QueryBuilders.matchAllQuery();
			}
			else {
				criteriaBuilder = buildQuery(criteria);
			}
			
			final String trimmedFreetext = ItemIndex.trimAndLowercaseFreetext(freeText);
			
			if (TYPE_HANDLING.hasQueryTypeFilter() || trimmedFreetext != null) {
				// Add to a boolean query also specifying types
				final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

				boolQuery.must(criteriaBuilder);

				if (types.size() == 1) {
					boolQuery.must(TYPE_HANDLING.createQueryTypeFilter(types.get(0)));
				}
				else {
					final BoolQueryBuilder typesQuery = QueryBuilders.boolQuery();
		
					for (Class<? extends Item> type : types) {
						typesQuery.should(TYPE_HANDLING.createQueryTypeFilter(type));
					}
				}

				if (trimmedFreetext != null) {
					boolQuery.must(createFreetextQuery(types, trimmedFreetext));
				}

				queryBuilder = boolQuery;
			}
			else {
				queryBuilder = criteriaBuilder;
			}
	
			sourceBuilder.query(queryBuilder);

			if (sortOrder != null && !sortOrder.isEmpty()) {

				for (SortAttributeAndOrder so : sortOrder) {

					final String fieldName = ItemIndex.fieldName(so.getAttribute());
					final org.elasticsearch.search.sort.SortOrder esSortOrder;

					switch (so.getSortOrder()) {
					case ASCENDING:
						esSortOrder = org.elasticsearch.search.sort.SortOrder.ASC;
						break;
						
					case DESCENDING:
						esSortOrder = org.elasticsearch.search.sort.SortOrder.DESC;
						break;

					default:
						throw new IllegalArgumentException("Unknown sort order " + so.getSortOrder());
					}

					sourceBuilder.sort(fieldName, esSortOrder);
				}
			}
			
			if (facetAttributes != null && !facetAttributes.isEmpty()) {
				addAggregations(sourceBuilder, facetAttributes);
			}
	
			if (false) {
				try {
					final XContentBuilder content = JsonXContent.contentBuilder();
					
					sourceBuilder.toXContent(content, null);
					
					System.out.println("## request: " + content.string());
				}
				catch (IOException ex) {
					throw new IllegalStateException("Failed to create content builder", ex);
				}
			}
	
			final SearchResponse response;
			try {
				response = client.search(request);
			} catch (IOException ex) {
				throw new ItemIndexException("Failed to read index result", ex);
			}
			
			// TODO use ES paging? For now optimized to get all data
			
			cursor = new ESIndexSearchCursor(response, fieldAttributes);
		}

		return cursor;
	}
	
	private class ESIndexSearchCursor implements IndexSearchCursor {
		private final SearchResponse response;
		private final Set<ItemAttribute> fieldAttributes;
		
		public ESIndexSearchCursor(SearchResponse response, Set<ItemAttribute> fieldAttributes) {
			this.response = response;
			this.fieldAttributes = fieldAttributes;
		}
		
		private SearchHit [] getHits() {
			final SearchHits searchHits = response.getHits();
			
			final SearchHit [] hits = searchHits.getHits();
		
			return hits;
		}

		@Override
		public int getTotalMatchCount() {
			final SearchHits searchHits = response.getHits();

			if (searchHits.totalHits > Integer.MAX_VALUE) {
				throw new IllegalStateException("More than Integer.MAX_VALUE hits");
			}

			return (int)searchHits.totalHits;
		}
		
		@Override
		public List<SearchItem> getItemIDsAndTitles(int initialIdx, int count) {
			final SearchHit [] hits = getHits();

			final List<SearchItem> items = new ArrayList<>(Math.min(hits.length, count));

			for (int i = 0; i < count && (initialIdx + i) < hits.length; ++ i) {

				final FieldValues fieldValues;

				final SearchHit hit = hits[initialIdx + i];
				
				final Map<String, Object> sourceMap = hit.getSourceAsMap();
				final String title = (String)sourceMap.get(ItemIndex.fieldName(titleAttribute));

				@SuppressWarnings("unchecked")
				final List<Integer> thumbs = (List<Integer>)sourceMap.get(FIELD_THUMBS);
				
				final Integer thumbWidth;
				final Integer thumbHeight;
				if (thumbs != null && !thumbs.isEmpty()) {
					final int thumbEncoded = thumbs.get(0);
					
					thumbWidth = thumbEncoded >> 16;
					thumbHeight = thumbEncoded & 0x0000FFFF;
				}
				else {
					thumbWidth = null;
					thumbHeight = null;
				}
				
				if (this.fieldAttributes != null && !fieldAttributes.isEmpty()) {

					fieldValues = new FieldValues() {
						
						@Override
						public Object getValue(ItemAttribute attribute) {
							return sourceMap.get(ItemIndex.fieldName(attribute));
						}
					};
				}
				else {
					fieldValues = null;
				}

				items.add(new IndexSearchItem(hit.getId(), title, thumbWidth, thumbHeight, fieldValues));
			}
			
			return items;
		}
		
		@Override
		public List<String> getItemIDs(int initialIdx, int count) {
			final SearchHit [] hits = getHits();

			final List<String> ids = new ArrayList<>(Math.min(hits.length, count));
			
			for (int i = 0; i < count && (initialIdx + i) < hits.length; ++ i) {
				ids.add(hits[initialIdx + i].getId());
			}

			return ids;
		}

		@Override
		public ItemsFacets getFacets() {
			return makeFacets(response.getAggregations());
		}
	}
	
	
	private static ItemsFacets makeFacets(Aggregations aggregations) {
		

		// Issue: not able to differentiate on type here with only one ES type per index
		// How to handle?
		// - insert our own type field and filter on that
		// - split in multiple indices? May have to do anyways because of index sparsity, ie avoid unused fields 

		// Loop over all aggregations and figure type

		final List<TypeFacets> types = new ArrayList<>();
		
		for (Aggregation typeAggregation : aggregations.asList()) {

			final String typeName = splitAggregationName(typeAggregation.getName());
			
			final TypeInfo typeInfo = ItemTypes.getTypeByName(typeName);
			if (typeInfo == null) {
				throw new IllegalStateException("No type info for " + typeName);
			}
			
			final SingleBucketAggregation sb = (SingleBucketAggregation)typeAggregation;
			final List<IndexFacetedAttributeResult> attributes = new ArrayList<>();
			final Map<ItemAttribute, IndexFacetedAttributeResult> attributeResults = new HashMap<>();

			processSubAggregations(typeInfo, sb.getAggregations().asList(), attributes, attributeResults);
			
			final TypeFacets typeFacets = new TypeFacets(typeInfo.getType(), attributes);
			
			types.add(typeFacets);
		}
		
		return new ItemsFacets(types);
	}
	
	private static void processSubAggregations(
			TypeInfo typeInfo,
			Collection<Aggregation> aggregations,
			List<IndexFacetedAttributeResult> attributes,
			Map<ItemAttribute, IndexFacetedAttributeResult> attributeResults) {

		for (Aggregation subAggregation : aggregations) {
			
			final String attributeName = splitAggregationName(subAggregation.getName());
			
			final ItemAttribute attribute = typeInfo.getAttributes().getByName(attributeName);
			
			if (attribute == null) {
				throw new IllegalStateException("No attribute with name " + attributeName);
			}

			final IndexFacetedAttributeResult attributeResult;
			
			final IndexFacetedAttributeResult existingAttributeResult = attributeResults.get(attribute);
			
			if (subAggregation instanceof Range) {
				
				final Range range = (Range)subAggregation;
				final int expectedCounts = attribute.getRangeCount();

				final int [] matchCounts = new int[expectedCounts];
				
				// For simplicity EleasticSearch returns ranges in same order as query
				if (range.getBuckets().size() != expectedCounts) {
					throw new IllegalStateException("Different number of ranges in return");
				}
						
				for (int i = 0; i < expectedCounts; ++ i) {
					final Bucket bucket = range.getBuckets().get(i);

					if (bucket.getDocCount() > Integer.MAX_VALUE) {
						throw new IllegalStateException("bucket.getDocCount() > Integer.MAX_VALUE");
					}

					matchCounts[i] = (int)bucket.getDocCount();
				}

				attributeResult = new IndexRangeFacetedAttributeResult(attribute, matchCounts);
				
				attributeResults.put(attribute, attributeResult);
				
				if (existingAttributeResult != null) {
					// had 
				}
			}
			else if (subAggregation instanceof Terms) {
				final Terms terms = (Terms)subAggregation;
				
				final IndexSingleValueFacetedAttributeResult singleValueFacetedAttributeResult = FacetUtils.createSingleValueFacetedAttributeResult(attribute);
				attributeResult = singleValueFacetedAttributeResult;

				terms.getBuckets().forEach(b -> {
					if (b.getDocCount() > Integer.MAX_VALUE) {
						throw new IllegalStateException("b.getDocCount() > Integer.MAX_VALUE");
					}
					
					final Object value = b.getKey();

					final IndexSingleValueFacet singleValueFacet = FacetUtils.addSingleValueFacet(attribute, singleValueFacetedAttributeResult, value);

					final List<IndexFacetedAttributeResult> subAttributes = new ArrayList<>();
					final Map<ItemAttribute, IndexFacetedAttributeResult> subAttributeResults = new HashMap<>();

					processSubAggregations(typeInfo, b.getAggregations().asList(), subAttributes, subAttributeResults);

					singleValueFacet.setSubFacets(new ArrayList<>(subAttributeResults.values()));
				});
			}
			else if (subAggregation instanceof Missing) {
				final Missing missing = (Missing)subAggregation;

				if (missing.getDocCount() > Integer.MAX_VALUE) {
					throw new IllegalStateException("missing.getDocCount() > Integer.MAX_VALUE");
				}
				
				attributeResult = null;
				
				
				if (missing.getDocCount() > 0L) {
					final IndexFacetedAttributeResult missingAttributeResult = FacetUtils.assureResult(attribute, attributeResults);

					missingAttributeResult.addToNoAttributeValueCount((int)missing.getDocCount());

					attributeResults.put(attribute, missingAttributeResult);
				}
			}
			else {
				throw new UnsupportedOperationException("Unknown aggregation type: " + subAggregation.getClass());
			}

			if (attributeResult != null) {
				if (existingAttributeResult != null) {
					// missing-attribute was already found and added to hash, so update count from already found attribute before we put new value in hash
					// For opposite order (value attributes found first), attributeResult will be null in missing-case so will never reach here
					attributeResult.addToNoAttributeValueCount(existingAttributeResult.getNoAttributeValueCount());
				}
				
				attributeResults.put(attribute, attributeResult);
				attributes.add(attributeResult);
			}
		}
	}
	
	private static String splitAggregationName(String name) {
		
		if (name.startsWith(MISSING_PREFIX)) {
			name = name.substring(MISSING_PREFIX.length());
		}
		
		final String [] s = name.split("_");
		
		if (s.length != 2) {
			throw new IllegalStateException("Aggregation name not two parts: " + Arrays.toString(s));
		}
		
		if (!s[1].equals("agg")) {
			throw new IllegalStateException("Aggregation name not ending in agg");
		}

		return s[0];
	}

	private static QueryBuilder buildQuery(List<Criterium> criteria) {
		
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
	
		for (Criterium criterium : criteria) {
			final ItemAttribute attribute = criterium.getAttribute();
			final String fieldName = ItemIndex.fieldName(attribute);

			if (criterium instanceof ComparisonCriterium<?>) {
				final ESQueryAndOccur queryAndOccur = createComparisonQuery(criterium, fieldName);
				
				switch (queryAndOccur.occur) {
				case MUST:
					queryBuilder.must(queryAndOccur.query);
					break;
					
				case MUST_NOT:
					queryBuilder.mustNot(queryAndOccur.query);
					break;
					
				default:
					throw new UnsupportedOperationException("Unsupported occur: " + queryAndOccur.occur);
				}
			}
			else if (criterium instanceof InCriterium<?>) {
				final InCriterium<?> inCriterium = (InCriterium<?>)criterium;
				final QueryBuilder inQueryBuilder;

				final boolean hasSubCriteriaForAnyValues = inCriterium.getValues().stream().anyMatch(value -> value.getSubCritera() != null);

				if (hasSubCriteriaForAnyValues) {
					final BoolQueryBuilder booleanQuery = QueryBuilders.boolQuery();
					
					// Must create query for each test, then create sub-query if necessary
					for (InCriteriumValue<?> value : inCriterium.getValues()) {
						
						final QueryBuilder valueQuery = QueryBuilders.termQuery(fieldName, convertValue(attribute, value.getValue()));
						
						final QueryBuilder toAdd;
						if (value.getSubCritera() != null) {
							
							// Should have one of the subcritera
							final BoolQueryBuilder sub = QueryBuilders.boolQuery();
							
							// must for both since we have to match both value and sub-criteria for this to be matched
							sub.must(valueQuery);
							
							@SuppressWarnings({ "unchecked", "rawtypes" })
							final List<Criterium> lc = (List)value.getSubCritera();
							
							sub.must(buildQuery(lc));
							
							toAdd = sub;
						}
						else {
							// No sub-query so just add valueQuery directly
							toAdd = valueQuery;
						}

 						booleanQuery.should(toAdd);
					}

					inQueryBuilder = booleanQuery;
				}
				else {
					// No sub-criteria, just add terms, ES will pass terms as array, eg
					// "terms": {
		            //  "make": [
		            //           "Burton",
					//           "Jones"
		            //         ],
		            //         "boost": 1.0
		            //       }
				
					inQueryBuilder = makeTermsQuery(inCriterium);
					
					
				}
				queryBuilder.must(inQueryBuilder);
			}
			else if (criterium instanceof NoValueCriterium) {
				queryBuilder.mustNot(QueryBuilders.existsQuery(fieldName));
			}
			else {
				throw new UnsupportedOperationException("Unknown criteria type " + criterium.getClass());
			}
		}

		return queryBuilder;
	}
	
	private static QueryBuilder makeTermsQuery(InCriterium<?> inCriterium) {
		
		final QueryBuilder inQueryBuilder;
		final ItemAttribute attribute = inCriterium.getAttribute();
		final String fieldName = ItemIndex.fieldName(attribute);

		switch (attribute.getAttributeType()) {
		case BOOLEAN:
		case STRING:
		case INTEGER:
		case LONG:
		case DATE:
			final Object [] strings = convertValues(inCriterium, o -> (String)o);
			inQueryBuilder = QueryBuilders.termsQuery(fieldName, strings);
			break;
			
		case DECIMAL:
			// Must convert to double
			final Object [] doubles = convertValues(inCriterium, o -> ((BigDecimal)o).doubleValue());
			inQueryBuilder = QueryBuilders.termsQuery(fieldName, doubles);
			break;

		case ENUM:
			final Object [] enums = convertValues(inCriterium, o -> ((Enum<?>)o).name());
			inQueryBuilder = QueryBuilders.termsQuery(fieldName, enums);
			break;
			
		
		default:
			throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
		}

		return inQueryBuilder;
	}

	private static <T extends Comparable<T>> Object [] convertValues(InCriterium<T> criterium, Function<T, Object> convert) {

		final List<InCriteriumValue<T>> values = criterium.getValues();
		final Object [] result = new Object[values.size()];
		
		for (int i = 0; i < values.size(); ++ i) {
			result[i] = convert.apply(values.get(i).getValue());
		}

		return result;
	}
	
	private static Object convertValue(ItemAttribute attribute, Object value) {
		
		final Object result;
		
		switch (attribute.getAttributeType()) {
		case BOOLEAN:
		case STRING:
		case INTEGER:
		case LONG:
		case DATE:
			// Just keep same as input
			result = value;
			break;
			
		case DECIMAL:
			// Must convert to double
			result = ((BigDecimal)value).doubleValue();
			break;

		case ENUM:
			result = ((Enum<?>)value).name();
			break;
		
		default:
			throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
		}

		return result;
	}
	
	
	enum ESOccur {
		MUST,
		MUST_NOT
	};

	private static class ESQueryAndOccur {
		private final QueryBuilder query;
		private final ESOccur occur;
		
		ESQueryAndOccur(QueryBuilder query, ESOccur occur) {
			this.query = query;
			this.occur = occur;
		}
	}

	private static ESQueryAndOccur createComparisonQuery(Criterium criterium, String fieldName) {
		final QueryBuilder query;
		final ESOccur occur;

		final ComparisonOperator comparisonOperator = ((ComparisonCriterium<?>) criterium).getComparisonOperator();
		
		if (criterium instanceof StringCriterium) {
			final String value = ((StringCriterium)criterium).getValue();
			
			query = QueryBuilders.termQuery(fieldName, value);
			occur = ESOccur.MUST;
		}
		else if (criterium instanceof IntegerCriterium) {
			final Integer value = ((IntegerCriterium)criterium).getValue();

			switch (comparisonOperator) {
			case EQUALS:
				query = QueryBuilders.termQuery(fieldName, value);
				occur = ESOccur.MUST;
				break;
				
			case NOT_EQUALS:
				query = QueryBuilders.termQuery(fieldName, value);
				occur = ESOccur.MUST_NOT;
				break;
			
			case LESS_THAN:
				query = QueryBuilders.rangeQuery(fieldName).from(null).to(value, false);
				occur = ESOccur.MUST;
				break;
				
			case LESS_THAN_OR_EQUALS:
				query = QueryBuilders.rangeQuery(fieldName).from(null).to(value, false);
				occur = ESOccur.MUST;
				break;
				
			case GREATER_THAN:
				query = QueryBuilders.rangeQuery(fieldName).from(value, false).to(null);
				occur = ESOccur.MUST;
				break;
			
			case GREATER_THAN_OR_EQUALS:
				query = QueryBuilders.rangeQuery(fieldName).from(value, true).to(null);
				occur = ESOccur.MUST;
				break;
				
			default:
				throw new UnsupportedOperationException("Unknown comparison operator: " + comparisonOperator);
			}
		}
		else if (criterium instanceof DecimalCriterium) {
			final BigDecimal value = ((DecimalCriterium)criterium).getValue();
			
			switch (comparisonOperator) {
			case EQUALS:
				query = QueryBuilders.termQuery(fieldName, value.doubleValue());
				occur = ESOccur.MUST;
				break;
				
			case NOT_EQUALS:
				query = QueryBuilders.termQuery(fieldName, value.doubleValue());
				occur = ESOccur.MUST_NOT;
				break;
			
			case LESS_THAN:
				query = QueryBuilders.rangeQuery(fieldName).from(null).to(value.doubleValue(), false);
				occur = ESOccur.MUST;
				break;
				
			case LESS_THAN_OR_EQUALS:
				query = QueryBuilders.rangeQuery(fieldName).from(null).to(value.doubleValue(), false);
				occur = ESOccur.MUST;
				break;
				
			case GREATER_THAN:
				query = QueryBuilders.rangeQuery(fieldName).from(value.doubleValue(), false).to(null);
				occur = ESOccur.MUST;
				break;
			
			case GREATER_THAN_OR_EQUALS:
				query = QueryBuilders.rangeQuery(fieldName).from(value.doubleValue(), true).to(null);
				occur = ESOccur.MUST;
				break;
				
			default:
				throw new UnsupportedOperationException("Unknown comparison operator: " + comparisonOperator);
			}
		}
		else {
			throw new UnsupportedOperationException("Unknown criterium");
		}

		return new ESQueryAndOccur(query, occur);
	}

	private static void addAggregations(SearchSourceBuilder sourceBuilder, Set<ItemAttribute> facets) {
		// Create a bucket aggregation for each attribute and then a count metric-aggregation within each
		
		final Set<Class<? extends Item>> distinctTypes = facets.stream()
				.filter(f -> f.isFaceted())
				.map(f -> f.getItemType())
				.collect(Collectors.toSet());

		// Make aggregations for all attributes of a particular type
		for (Class<? extends Item> type : distinctTypes) {
			
			final Consumer<AggregationBuilder> addAttributeAggregation;
			final AggregationBuilder typeFilter;
			if (TYPE_HANDLING.hasAggregationTypeFilter()) {
				typeFilter = TYPE_HANDLING.createAggregationTypeFilter(type);

				addAttributeAggregation = a -> typeFilter.subAggregation(a);
			}
			else {
				typeFilter = null;
				
				addAttributeAggregation = a -> sourceBuilder.aggregation(a);
			}

			addAggregationForAttributes(type, facets, null, addAttributeAggregation);
			

			if (TYPE_HANDLING.hasAggregationTypeFilter()) {
				sourceBuilder.aggregation(typeFilter);
			}
		}
	}
	
	private static void addAggregationForAttributes(
			Class<? extends Item> type,
			Collection<ItemAttribute> facets,
			ItemAttribute superAttribute,
			Consumer<AggregationBuilder> addAttributeAggregation) {
		
		for (ItemAttribute facet : facets) {
			
			if (!facet.getItemType().equals(type)) {
				continue;
			}
			
			if (!facet.isFaceted()) {
				continue;
			}
			
			if (superAttribute == null) {
				if (facet.getFacetSuperAttribute() != null) {
					continue;
				}
			}
			else {
				if (!superAttribute.getName().equals(facet.getFacetSuperAttribute())) { // FacetSuperAttribute may be null here
					continue;
				}
			}
			
			final String fieldName = facet.getName();
			final String aggregationName = fieldName + "_agg";
			
			if (facet.isSingleValue()) {

				final TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(aggregationName)
						.field(fieldName)
						.size(Integer.MAX_VALUE);
				
				addAttributeAggregation.accept(termsAggregationBuilder);

				final List<ItemAttribute> subAttributes = facets.stream()
						.filter(attr -> facet.getName().equals(attr.getFacetSuperAttribute()) && attr.isFaceted())
						.collect(Collectors.toList());
				
				if (!subAttributes.isEmpty()) {
					// Add for all that has this as super attribute
					final Consumer<AggregationBuilder> addSubAggregation = a -> termsAggregationBuilder.subAggregation(a);
					
					addAggregationForAttributes(type, facets, facet, addSubAggregation);
				}
			}
			else if (facet.isRange()) {
				
				final RangeAggregationBuilder rangeAggregation = AggregationBuilders.range(aggregationName).field(fieldName);
				
				if (facet.getIntegerRanges() != null) {
					
					for (int i = 0; i < facet.getIntegerRanges().length; ++ i) {
						
						final FacetedAttributeIntegerRange range = facet.getIntegerRanges()[i];
						final String key = String.valueOf(i);

						if (range.getLower() == null) {
							rangeAggregation.addUnboundedTo(key, range.getUpper());
						}
						else if (range.getUpper() == null) {
							rangeAggregation.addUnboundedFrom(key, range.getLower());
						}
						else {
							rangeAggregation.addRange(key,  range.getLower(), range.getUpper());
						}
					}
				}
				else if (facet.getDecimalRanges() != null) {
					for (int i = 0; i < facet.getDecimalRanges().length; ++ i) {
						
						final FacetedAttributeDecimalRange range = facet.getDecimalRanges()[i];
						final String key = String.valueOf(i);

						if (range.getLower() == null) {
							rangeAggregation.addUnboundedTo(key, range.getUpper().doubleValue());
						}
						else if (range.getUpper() == null) {
							rangeAggregation.addUnboundedFrom(key, range.getLower().doubleValue());
						}
						else {
							rangeAggregation.addRange(key,  range.getLower().doubleValue(), range.getUpper().doubleValue());
						}
					}
				}
				else {
					throw new IllegalStateException("Neither integer nor decimal ranges: " + facet.getName());
				}
				
				addAttributeAggregation.accept(rangeAggregation);
			}
			else {
				throw new IllegalStateException("Neither single value nor range: " + facet.getName());
			}
			
			// Add an aggregation for elements that has no value set for this field
			addAttributeAggregation.accept(AggregationBuilders.missing(MISSING_PREFIX + fieldName + "_agg").field(fieldName));
		}
		
	}
	
	private void createIndexIfNotExists() throws ItemIndexException {
		
		final GetIndexRequest getIndex = new GetIndexRequest();
		
		getIndex.indices(INDEX_NAME);
		
		final CreateIndexRequest create = new CreateIndexRequest(INDEX_NAME);
		
		// Add all field types
		final Map<String, String> fieldTypes = new HashMap<>();
		
		final StringBuilder sb = new StringBuilder();
		
		sb.append("{ ").append('"').append("mappings").append('"').append(" : {");

		boolean firstType = true;

		for (String typeName : TYPE_HANDLING.getCreateIndexTypes(ItemTypes.getAllTypesSet())) {
			if (firstType) {
				firstType = false;
			}
			else {
				sb.append(", ");
			}

			TYPE_HANDLING.getCreateIndexAttributes(typeName).forEach(attribute -> {
				final String esFieldType = getESFieldType(attribute.getAttributeType());
				
				if (attribute.isFreetext()) {
					// Index freetext field as well
					final String freetextFieldName = ItemIndex.freetextFieldName(attribute);

					fieldTypes.put(freetextFieldName, "text");
				}

				fieldTypes.put(ItemIndex.fieldName(attribute), esFieldType);
			});
			
			final Map<String, String> custom = TYPE_HANDLING.createIndexCustomFields(typeName);
			
			if (custom != null) {
				fieldTypes.putAll(custom);
			}

			sb.append('"').append(typeName).append('"').append(" : { ");
			sb.append('"').append("properties").append('"').append(" : { ");
			
			boolean firstAttribute = true;

			for (Map.Entry<String, String> fieldType : fieldTypes.entrySet()) {
				
				final String fieldName = fieldType.getKey();

				if (firstAttribute) {
					firstAttribute = false;
				}
				else {
					sb.append(", ");
				}
				
				sb.append('"').append(fieldName).append('"').append(" : { ")
					.append('"').append("type").append('"').append(" : ").append('"').append(fieldType.getValue()).append('"')
					.append(" }");
			}
			
			
			sb.append(" }");
			sb.append(" }");
			
			/*
			final Map<String, Object> properties = new HashMap<>();
			properties.put("properties", new HashMap<>(fieldTypes));
			
			create.mapping(typeName, new Hash);
			
			fieldTypes.clear(); // reuse map for next type
			*/
		}

		sb.append(" }");

		sb.append(" }");

		final String json = sb.toString();

		create.source(json, XContentType.JSON);
		
		if (false) {
			try {
				final XContentBuilder content = JsonXContent.contentBuilder();
				
				create.settings(new HashMap<>());
				
				create.toXContent(content, params);
				
				System.out.println("## create request: " + content.string());
			}
			catch (IOException ex) {
				throw new IllegalStateException("Faield to create content builder", ex);
			}
		}
		

		
		try {
			client.indices().create(create);
		} catch (ElasticsearchStatusException ex) {
			if (	ex.getRootCause() != null
				 && ex.getRootCause().getMessage() != null
				 && ex.getRootCause().getMessage().contains("resource_already_exists_exception")) {

				// Already exists, do nothing
			}
			else {
				throw ex;
			}
		} catch (IOException ex) {
			throw new ItemIndexException("Failed to create index", ex);
		}
	}
	
	private static String getESFieldType(AttributeType attributeType) {
		final String esFieldType;
		
		switch (attributeType) {
		case STRING:
		case ENUM:
			esFieldType = "keyword";
			break;
		
		case INTEGER:
			esFieldType = "integer";
			break;
			
		case LONG:
			esFieldType = "long";
			break;
			
		case DECIMAL:
			esFieldType = "double";
			break;
			
		case DATE:
			esFieldType = "date";
			break;
			
		case BOOLEAN:
			esFieldType = "boolean";
			break;
			
		default:
			throw new UnsupportedOperationException("Unknown attribute type " + attributeType);
		}

		return esFieldType;
	}
	
	private static final ToXContent.Params params = new ToXContent.Params() {
		
		@Override
		public Boolean paramAsBoolean(String key, Boolean defaultValue) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean paramAsBoolean(String key, boolean defaultValue) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public String param(String key, String defaultValue) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String param(String key) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	private static String getTypeName(Class<? extends Item> type) {
		return TYPE_HANDLING.getESTypeName(type);
	}
}
