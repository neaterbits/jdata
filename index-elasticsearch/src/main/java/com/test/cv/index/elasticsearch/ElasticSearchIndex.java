package com.test.cv.index.elasticsearch;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.test.cv.common.ItemId;
import com.test.cv.index.IndexSearchCursor;
import com.test.cv.index.ItemIndex;
import com.test.cv.index.ItemIndexException;
import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.ItemAttributeValue;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.search.criteria.Criterium;

public class ElasticSearchIndex implements ItemIndex {

	private final RestHighLevelClient client;
	

	private static final String INDEX_NAME = "items";

	private static final String FIELD_USER_ID = "userId";
	private static final String FIELD_THUMBS = "thumbs";
	
	public ElasticSearchIndex(String endpointUrl) {
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
		
		request.storedFields(FIELD_THUMBS);
		
		final GetResponse response;
		try {
			response = client.get(request);
		} catch (IOException ex) {
			throw new ItemIndexException("Failed to get item with ID " + itemId, ex);
		}
		
		// Encoded width/height as longs
		final DocumentField thumbsField = response.getField(FIELD_THUMBS);
		
		final Integer [] integers;
		
		if (thumbsField != null) {
			int [] thumbSizes = thumbsField.getValue();
	
			integers = new Integer[thumbSizes.length];
			for (int i = 0; i < thumbSizes.length; ++ i) {
				integers[i] = thumbSizes[i];
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
		
		updateThumbs(itemId, null, moved);
	}

	@Override
	public ItemId[] expandToItemIdUserId(String[] itemIds) throws ItemIndexException {

		final SearchRequest request = new SearchRequest(INDEX_NAME);
		
		final Map<String, Integer> idToIndex = new HashMap<>();
		
		for (int i = 0; i < itemIds.length; ++ i) {
			idToIndex.put(itemIds[i], i);
		}
 		
		final SearchSourceBuilder sourceBuilder = request.source();
		
		sourceBuilder.fetchSource(new String [] { "*." + FIELD_USER_ID }, null);

		sourceBuilder.query(QueryBuilders.idsQuery().addIds(itemIds));

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
			final String userId = hit.field(FIELD_USER_ID).getValue();
			
			final int index = idToIndex.get(id);
			
			if (result[index] != null) {
				throw new IllegalStateException("Multple entries at " +  index + " for id " + id);
			}
			
			result[index] = new ItemId(userId, id);
		}

		return null;
	}

	@Override
	public IndexSearchCursor search(String freeText, List<Criterium> criteria, Set<ItemAttribute> facetAttributes)
			throws ItemIndexException {

		throw new UnsupportedOperationException("TODO - search");
	}
}
