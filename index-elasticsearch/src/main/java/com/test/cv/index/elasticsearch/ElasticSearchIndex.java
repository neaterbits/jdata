package com.test.cv.index.elasticsearch;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.Range.Bucket;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import com.test.cv.common.ItemId;
import com.test.cv.index.IndexSearchCursor;
import com.test.cv.index.IndexSearchItem;
import com.test.cv.index.ItemIndex;
import com.test.cv.index.ItemIndexException;
import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.ItemAttributeValue;
import com.test.cv.model.attributes.AttributeType;
import com.test.cv.model.attributes.facets.FacetedAttributeDecimalRange;
import com.test.cv.model.attributes.facets.FacetedAttributeIntegerRange;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.model.items.TypeInfo;
import com.test.cv.search.SearchItem;
import com.test.cv.search.criteria.ComparisonCriterium;
import com.test.cv.search.criteria.ComparisonOperator;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.criteria.DecimalCriterium;
import com.test.cv.search.criteria.InCriterium;
import com.test.cv.search.criteria.IntegerCriterium;
import com.test.cv.search.criteria.StringCriterium;
import com.test.cv.search.facets.FacetUtils;
import com.test.cv.search.facets.IndexFacetedAttributeResult;
import com.test.cv.search.facets.IndexRangeFacetedAttributeResult;
import com.test.cv.search.facets.IndexSingleValueFacetedAttributeResult;
import com.test.cv.search.facets.ItemsFacets;
import com.test.cv.search.facets.TypeFacets;

import static com.test.cv.common.ArrayUtil.convertArray;

// TODO use multiple indices, one per type? instead of merging all into one index. Multiple types per index is obsleted in >= 6.x
// See https://www.elastic.co/guide/en/elasticsearch/reference/5.3/general-recommendations.html#sparsity


public class ElasticSearchIndex implements ItemIndex {

	private final RestHighLevelClient client;
	

	private static final String INDEX_NAME = "items";

	private static final String FIELD_USER_ID = "userId";
	private static final String FIELD_THUMBS = "thumbs";
	
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

			sb.append("  ").append('"').append(attribute.getName()).append('"').append(" : ");

			final Object v = value.getValue();
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
	public IndexSearchCursor search(String freeText, List<Criterium> criteria, Set<ItemAttribute> facetAttributes)
			throws ItemIndexException {

		// Must add both query and also aggregations if necessary
		final SearchRequest request = new SearchRequest(INDEX_NAME);

		final SearchSourceBuilder sourceBuilder = request.source();
		
		final QueryBuilder queryBuilder;
		
		if (criteria == null) {
			queryBuilder = QueryBuilders.matchAllQuery();
		}
		else {
			queryBuilder = buildQuery(criteria);
		}

		sourceBuilder.query(queryBuilder);
		
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
		
		final SearchHits searchHits = response.getHits();
		
		final SearchHit [] hits = searchHits.getHits();
		
		// TODO use ES paging? For now optimized to get all data
		
		return new IndexSearchCursor() {
			
			@Override
			public int getTotalMatchCount() {
				
				if (searchHits.totalHits > Integer.MAX_VALUE) {
					throw new IllegalStateException("More than Integer.MAX_VALUE hits");
				}

				return (int)searchHits.totalHits;
			}
			
			@Override
			public List<SearchItem> getItemIDsAndTitles(int initialIdx, int count) {

				final List<SearchItem> items = new ArrayList<>(Math.min(hits.length, count));
				
				for (int i = 0; i < count && (initialIdx + i) < hits.length; ++ i) {

					final SearchHit hit = hits[initialIdx + i];
					
					final Map<String, Object> sourceMap = hit.getSourceAsMap();
					final String title = (String)sourceMap.get(titleAttribute.getName());

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

					items.add(new IndexSearchItem(hit.getId(), title, thumbWidth, thumbHeight));
				}
				
				return items;
			}
			
			@Override
			public List<String> getItemIDs(int initialIdx, int count) {
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
		};
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
			
			for (Aggregation subAggregation : sb.getAggregations().asList()) {
				final MultiBucketsAggregation sub = (MultiBucketsAggregation)subAggregation;
				
				final String attributeName = splitAggregationName(sub.getName());
				
				final ItemAttribute attribute = typeInfo.getAttributes().getByName(attributeName);
				
				if (attribute == null) {
					throw new IllegalStateException("No attribute with name " + attributeName);
				}

				final IndexFacetedAttributeResult attributeResult;
				
				if (sub instanceof Range) {
					
					final Range range = (Range)sub;

					final int expectedCounts = attribute.getIntegerRanges() != null
							? attribute.getIntegerRanges().length
							: attribute.getDecimalRanges().length;
					
					final int [] matchCounts = new int[expectedCounts];
					
					// For simplicity EleasticSearch returns ranges in same order as query
					if (range.getBuckets().size() != expectedCounts) {
						throw new IllegalStateException("Different number or rages in return");
					}
							
					for (int i = 0; i < expectedCounts; ++ i) {
						final Bucket bucket = range.getBuckets().get(i);

						if (bucket.getDocCount() > Integer.MAX_VALUE) {
							throw new IllegalStateException("bucket.getDocCount() > Integer.MAX_VALUE");
						}

						matchCounts[i] = (int)bucket.getDocCount();
					}

					attributeResult = new IndexRangeFacetedAttributeResult(attribute, matchCounts);
				}
				else if (sub instanceof Terms) {
					final Terms terms = (Terms)sub;
					
					final IndexSingleValueFacetedAttributeResult singleValueFacetedAttributeResult = FacetUtils.createSingleValueFacetedAttributeResult(attribute);
					attributeResult = singleValueFacetedAttributeResult;

					terms.getBuckets().forEach(b -> {
						if (b.getDocCount() > Integer.MAX_VALUE) {
							throw new IllegalStateException("b.getDocCount() > Integer.MAX_VALUE");
						}
						
						final Object value = b.getKey();
						
						FacetUtils.addSingleValueFacet(attribute, singleValueFacetedAttributeResult, value);
					});
					
				}
				else {
					throw new UnsupportedOperationException("Unknown aggregation type: " + sub.getClass());
				}
				
				attributes.add(attributeResult);
			}
			
			final TypeFacets typeFacets = new TypeFacets(typeInfo.getType(), attributes);
			
			types.add(typeFacets);
		}
		
		return new ItemsFacets(types);
	}
	
	private static String splitAggregationName(String name) {
		final String [] s = name.split("_");
		
		if (s.length != 2) {
			throw new IllegalStateException("Aggregation name not two parts: " + Arrays.toString(s));
		}
		
		if (!s[1].equals("agg")) {
			throw new IllegalStateException("Aggregation name not ending in .agg");
		}

		return s[0];
	}

	private static QueryBuilder buildQuery(List<Criterium> criteria) {
		
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
	
		for (Criterium criterium : criteria) {
			final ItemAttribute attribute = criterium.getAttribute();
			final String fieldName = attribute.getName();

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
				
				switch (attribute.getAttributeType()) {
				case BOOLEAN:
				case STRING:
				case INTEGER:
				case LONG:
				case DATE:
					inQueryBuilder = QueryBuilders.termsQuery(fieldName, inCriterium.getValues());
					break;
					
				case DECIMAL:
					// Must convert to double
					final Object [] doubles = convertArray(
							inCriterium.getValues(),
							length -> new Object[length],
							o -> ((BigDecimal)o).doubleValue());
					inQueryBuilder = QueryBuilders.termsQuery(fieldName, doubles);
					break;

				case ENUM:
					final Object [] enums = convertArray(
							inCriterium.getValues(),
							length -> new Object[length],
							o -> ((Enum<?>)o).name());
					inQueryBuilder = QueryBuilders.termsQuery(fieldName, enums);
					break;
					
				
				default:
					throw new UnsupportedOperationException("Unknown attribute type " + attribute.getAttributeType());
				}
				
				queryBuilder.must(inQueryBuilder);
			}
			else {
				throw new UnsupportedOperationException("Unknown criteria type " + criterium.getClass());
			}
		}

		return queryBuilder;
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
			
			final Consumer<AggregationBuilder> addAddAttributeAggregation;
			final AggregationBuilder typeFilter;
			if (TYPE_HANDLING.hasTypeFilter()) {
				typeFilter = TYPE_HANDLING.createTypeFilter(type);

				addAddAttributeAggregation = a -> typeFilter.subAggregation(a);
			}
			else {
				typeFilter = null;
				
				addAddAttributeAggregation = a -> sourceBuilder.aggregation(a);
			}

			for (ItemAttribute facet : facets) {
				
				if (!facet.getItemType().equals(type)) {
					continue;
				}
				
				if (!facet.isFaceted()) {
					continue;
				}
				
				
				final String fieldName = facet.getName();
				final String aggregationName = fieldName + "_agg";
				
				if (facet.isSingleValue()) {
					addAddAttributeAggregation.accept(AggregationBuilders.terms(aggregationName).field(fieldName).size(Integer.MAX_VALUE));
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
					
					addAddAttributeAggregation.accept(rangeAggregation);
				}
				else {
					throw new IllegalStateException("Neither single value nor range: " + facet.getName());
				}
			}

			if (TYPE_HANDLING.hasTypeFilter()) {
				sourceBuilder.aggregation(typeFilter);
			}
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

		for (String typeName : TYPE_HANDLING.getCreateIndexTypes(ItemTypes.getTypeClasses())) {
			if (firstType) {
				firstType = false;
			}
			else {
				sb.append(", ");
			}

			TYPE_HANDLING.getCreateIndexAttributes(typeName).forEach(attribute -> {
				final String esFieldType = getESFieldType(attribute.getAttributeType());
				
				fieldTypes.put(attribute.getName(), esFieldType);
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
