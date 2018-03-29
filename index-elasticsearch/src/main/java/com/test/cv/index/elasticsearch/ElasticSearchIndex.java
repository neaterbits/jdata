package com.test.cv.index.elasticsearch;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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
import com.test.cv.model.items.ItemTypes;
import com.test.cv.search.SearchItem;
import com.test.cv.search.criteria.ComparisonCriterium;
import com.test.cv.search.criteria.ComparisonOperator;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.criteria.DecimalCriterium;
import com.test.cv.search.criteria.InCriterium;
import com.test.cv.search.criteria.IntegerCriterium;
import com.test.cv.search.criteria.StringCriterium;
import com.test.cv.search.facets.ItemsFacets;

import static com.test.cv.common.ArrayUtil.convertArray;


public class ElasticSearchIndex implements ItemIndex {

	private final RestHighLevelClient client;
	

	private static final String INDEX_NAME = "items";

	private static final String FIELD_USER_ID = "userId";
	private static final String FIELD_THUMBS = "thumbs";
	
	// Name of field to represent title
	private final ItemAttribute titleAttribute;
	
	public ElasticSearchIndex(String endpointUrl, ItemAttribute titleAttribute) {

		if (titleAttribute == null) {
			throw new IllegalArgumentException("titleAttribute == null");
		}

		this.titleAttribute = titleAttribute;

		this.client = new RestHighLevelClient(RestClient.builder(new HttpHost(endpointUrl)));
	}
	
	@Override
	public void close() throws Exception {
		client.close();
	}

	@Override
	public void indexItemAttributes(String userId, Class<? extends Item> itemType, String typeName,
			List<ItemAttributeValue<?>> attributeValues) throws ItemIndexException {

		final String type = ItemTypes.getTypeName(itemType);
		
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
		
		final DeleteRequest request = new DeleteRequest(INDEX_NAME, ItemTypes.getTypeName(type), itemId);

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
		updateThumbs(itemId, ItemTypes.getTypeName(type), sizes);
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
		return getThumbs(itemId, ItemTypes.getTypeName(itemType));
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
		
		sourceBuilder.fetchSource(new String [] { titleAttribute.getName(), FIELD_THUMBS }, null);

		
		final QueryBuilder queryBuilder;
		
		if (criteria == null) {
			queryBuilder = QueryBuilders.matchAllQuery();
		}
		else {
			queryBuilder = buildQuery(criteria);
		}

		sourceBuilder.query(queryBuilder);

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
				throw new UnsupportedOperationException("TODO");
			}
		};
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

}
