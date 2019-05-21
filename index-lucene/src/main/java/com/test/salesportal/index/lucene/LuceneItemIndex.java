package com.test.salesportal.index.lucene;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.test.salesportal.common.ItemId;
import com.test.salesportal.index.IndexSearchCursor;
import com.test.salesportal.index.IndexSearchItem;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.index.ItemIndexException;
import com.test.salesportal.model.items.BooleanAttributeValue;
import com.test.salesportal.model.items.DateAttributeValue;
import com.test.salesportal.model.items.DecimalAttributeValue;
import com.test.salesportal.model.items.DistinctAttribute;
import com.test.salesportal.model.items.EnumAttributeValue;
import com.test.salesportal.model.items.IntegerAttributeValue;
import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.ItemAttributeValue;
import com.test.salesportal.model.items.LongAttributeValue;
import com.test.salesportal.model.items.PropertyAttribute;
import com.test.salesportal.model.items.SortAttribute;
import com.test.salesportal.model.items.SortAttributeAndOrder;
import com.test.salesportal.model.items.StringAttributeValue;
import com.test.salesportal.model.items.TZDateAttributeValue;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.attributes.AttributeType;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.search.FieldValues;
import com.test.salesportal.search.SearchItem;
import com.test.salesportal.search.criteria.ComparisonCriterium;
import com.test.salesportal.search.criteria.ComparisonOperator;
import com.test.salesportal.search.criteria.Criterium;
import com.test.salesportal.search.criteria.DecimalCriterium;
import com.test.salesportal.search.criteria.DecimalRange;
import com.test.salesportal.search.criteria.DecimalRangesCriterium;
import com.test.salesportal.search.criteria.InCriterium;
import com.test.salesportal.search.criteria.InCriteriumValue;
import com.test.salesportal.search.criteria.IntegerCriterium;
import com.test.salesportal.search.criteria.IntegerRange;
import com.test.salesportal.search.criteria.IntegerRangesCriterium;
import com.test.salesportal.search.criteria.NoValueCriterium;
import com.test.salesportal.search.criteria.Range;
import com.test.salesportal.search.criteria.RangesCriterium;
import com.test.salesportal.search.criteria.StringCriterium;
import com.test.salesportal.search.facets.FacetUtils;
import com.test.salesportal.search.facets.ItemsFacets;
import com.test.salesportal.search.facets.SortUtils;

public class LuceneItemIndex implements ItemIndex {
	
	// Hack to be able to match documents that do not have a value for a field, eg if selected 'Other' in a facet menu
	private static final String STRING_NONE = "__no_value__";
	private static final int INTEGER_NONE = -1;
	private static final long LONG_NONE = -1L;
	private static final double DOUBLE_NONE = -1.0;
	private static final int BOOLEAN_NONE = -1;
	private static final String ENUM_NONE = STRING_NONE; // Enums are stored as strings
	private static final long DATE_NONE = -1L;
	private static final String TZDATE_NONE = STRING_NONE;

	private static final String THUMBS_FIELD = "thumbs";
	private static final String TYPE_FIELD = "type";
	private static final String USERID_FIELD = "userId";

	private static final DateTimeFormatter TZDATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	
	private final ItemTypes itemTypes;
	
	private final Directory directory;
	private IndexWriter writer;
	private DirectoryReader reader;
	
	public LuceneItemIndex(String indexPath, ItemTypes itemTypes) throws IOException {
		this(FSDirectory.open(new File(indexPath).toPath()), itemTypes);
	}

	public LuceneItemIndex(Directory directory, ItemTypes itemTypes) throws IOException {

		if (itemTypes == null) {
			throw new IllegalArgumentException("itemTypes == null");
		}
		
		this.itemTypes = itemTypes;
		this.directory = directory;

		final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		final IndexWriter w = new IndexWriter(directory, config);

		// close to create empty index
		w.close();

		final IndexWriterConfig config2 = new IndexWriterConfig(new StandardAnalyzer());

		this.writer = new IndexWriter(directory, config2);

		this.reader = DirectoryReader.open(writer);
	}

	@Override
	public synchronized void indexItemAttributes(String userId, Class<? extends Item> itemType, String typeName, List<ItemAttributeValue<?>> attributeValues) throws ItemIndexException {
		final Document document = new Document();

		final boolean idFound = addToDocument(document, itemTypes.getTypeInfo(itemType), userId, attributeValues);

		if (!idFound) {
			throw new IllegalArgumentException("No ID attribute supplied");
		}

		try {
			writer.addDocument(document);
			writer.commit();
		} catch (IOException ex) {
			throw new ItemIndexException("Failed to write document for item", ex);
		}
	}
	
	private static boolean storeValue(ItemAttribute attribute) {
		return attribute.shouldStoreValueInSearchIndex() || attribute.isFaceted();
	}
	
	private boolean addToDocument(Document document, TypeInfo typeInfo, String userId, List<ItemAttributeValue<?>> attributeValues) {
		
		// Must have ID
		boolean idFound = false;
		
		document.add(new StringField(TYPE_FIELD, typeInfo.getTypeName(), Field.Store.YES));
		document.add(new StringField(USERID_FIELD, userId, Field.Store.YES));

		for (ItemAttributeValue<?> attributeValue : attributeValues) {
			
			final ItemAttribute attribute = attributeValue.getAttribute();
			
			if (isIdAttribute(attribute)) {
				idFound = true;
			}
			
			final Field field;
			
			final String fieldName = attributeValue.getAttribute().getName();
			final boolean storeValue = storeValue(attribute);

			//System.out.println("Indexing " + fieldName + " of type " + attributeValue.getClass().getSimpleName() + " with store=" + storeValue);
			StoredField storedField = null;
			
			if (attributeValue instanceof StringAttributeValue) {
				final String value = ((StringAttributeValue)attributeValue).getValue();
				
				if (value.equals(STRING_NONE)) {
					throw new IllegalArgumentException("Trying to index string no-value");
				}
				
				
				if (attribute.isFreetext()) {
					// Index freetext field
					final String freetextFieldName = ItemIndex.freetextFieldName(attribute);

					document.add(new TextField(freetextFieldName, value, Field.Store.NO));
				}
				
				// If not freetext or has sorting or faceting then store as StringField
				field = new StringField(fieldName, value,  storeValue ? Field.Store.YES : Field.Store.NO);
			}
			else if (attributeValue instanceof IntegerAttributeValue) {
				final int value = ((IntegerAttributeValue)attributeValue).getValue();

				if (value == INTEGER_NONE) {
					throw new IllegalArgumentException("Trying to index integer no-value");
				}

				field = new IntPoint(fieldName, value);
				
				if (storeValue) {
					storedField = new StoredField(fieldName, value);
				}
			}
			else if (attributeValue instanceof LongAttributeValue) {
				final long value = ((LongAttributeValue)attributeValue).getValue();

				if (value == LONG_NONE) {
					throw new IllegalArgumentException("Trying to index long no-value");
				}

				field = new LongPoint(fieldName, value);

				if (storeValue) {
					storedField = new StoredField(fieldName, value);
				}
			}
			else if (attributeValue instanceof DecimalAttributeValue) {
				final double value = ((DecimalAttributeValue)attributeValue).getValue().doubleValue();
				// TODO Lucene does not support decimals yet
				field = new DoublePoint(fieldName, value);
				
				if (value == DOUBLE_NONE) {
					throw new IllegalArgumentException("Trying to index double no-value");
				}

				if (storeValue) {
					storedField = new StoredField(fieldName, value);
				}
			}
			else if (attributeValue instanceof EnumAttributeValue) {
				final Enum<?> value = ((EnumAttributeValue)attributeValue).getValue();
				
				if (value.name().equals(ENUM_NONE)) {
					throw new IllegalArgumentException("Trying to index enum no-value");
				}

				
				field = new StringField(fieldName, value.name(), storeValue ? Field.Store.YES : Field.Store.NO);
			}
			else if (attributeValue instanceof BooleanAttributeValue) {
				// TODO possible to store as boolean? use int field for now
				final boolean value = ((BooleanAttributeValue)attributeValue).getValue().booleanValue();
				
				field = new IntPoint(fieldName, value ? 1 : 0);

				if (storeValue) {
					storedField = new StoredField(fieldName, value ? 1 : 0);
				}
			}
			else if (attributeValue instanceof DateAttributeValue) {
				final long value = ((DateAttributeValue)attributeValue).getValue().getTime();

				if (value == DATE_NONE) {
					throw new IllegalArgumentException("Trying to index date no-value");
				}

				field = new LongPoint(fieldName, value);

				if (storeValue) {
					storedField = new StoredField(fieldName, value);
				}
			}
			else if (attributeValue instanceof TZDateAttributeValue) {
				
				final OffsetDateTime offsetDateTime = ((TZDateAttributeValue)attributeValue).getValue();

				if (offsetDateTime == null) {
					throw new IllegalArgumentException("Trying to index tzdate no-value");
				}
				
				final String value = offsetDateTime.format(TZDATE_FORMATTER);

				field = new StringField(fieldName, value,  storeValue ? Field.Store.YES : Field.Store.NO);

				if (storeValue) {
					storedField = new StoredField(fieldName, value);
				}
			}
			else {
				throw new UnsupportedOperationException("Unknown attribute type : " + attributeValue.getClass());
			}
			
			document.add(field);
			
			if (storedField != null) {
				document.add(storedField);
			}
		}
		
		
		// Add no-value field for all fields that do not have a value
		typeInfo.getAttributes().forEach(attribute -> {
			if (!attributeValues.stream().anyMatch(value -> value.getAttribute().equals(attribute))) {
				// Does not exist, add none-value
				final boolean storeValue = storeValue(attribute);

				addNoAttributeValueToDocument(document, attribute, storeValue);
			}
		});
		
		return idFound;
	}
	
	private void addNoAttributeValueToDocument(Document document, ItemAttribute attribute, boolean storeValue) {
		
		final String fieldName = ItemIndex.fieldName(attribute);
		
		final Field field;
		
		final Field.Store stored = storeValue ? Field.Store.YES : Field.Store.NO;
		StoredField storedField = null;

		switch (attribute.getAttributeType()) {
		case STRING:
			field = new StringField(fieldName, STRING_NONE, stored);

			if (storeValue) {
				storedField = new StoredField(fieldName, STRING_NONE);
			}
			break;
			
		case INTEGER:
			field = new IntPoint(fieldName, INTEGER_NONE);

			if (storeValue) {
				storedField = new StoredField(fieldName, INTEGER_NONE);
			}
			break;
			
		case LONG:
			field = new LongPoint(fieldName, LONG_NONE);

			if (storeValue) {
				storedField = new StoredField(fieldName, LONG_NONE);
			}
			break;

		case DECIMAL:
			field = new DoublePoint(fieldName, DOUBLE_NONE);
			
			if (storeValue) {
				storedField = new StoredField(fieldName, DOUBLE_NONE);
			}
			break;
			
		case ENUM:
			field = new StringField(fieldName, ENUM_NONE, stored);

			if (storeValue) {
				storedField = new StoredField(fieldName, ENUM_NONE);
			}
			break;
			
		case BOOLEAN:
			field = new IntPoint(fieldName, BOOLEAN_NONE);
			
			if (storeValue) {
				storedField = new StoredField(fieldName, BOOLEAN_NONE);
			}
			break;

		case DATE:
			field = new LongPoint(fieldName, DATE_NONE);

			if (storeValue) {
				storedField = new StoredField(fieldName, DATE_NONE);
			}
			break;
			
		case TZDATE:
			field = new StringField(fieldName, TZDATE_NONE, stored);

			if (storeValue) {
				storedField = new StoredField(fieldName, TZDATE_NONE);
			}
			break;
			
		default:
			throw new UnsupportedOperationException("Unknown attribute type: " + attribute.getAttributeType());
		}

		document.add(field);

		if (storedField != null) {
			document.add(storedField);
		}
	}
	
	final List<ItemAttributeValue<?>> getValuesFromDocument(Document document, Set<ItemAttribute> attributes) {
		
		final List<ItemAttributeValue<?>> values = new ArrayList<>(attributes.size());

		for (ItemAttribute attribute : attributes) {
			final String fieldName = ItemIndex.fieldName(attribute);
			final IndexableField field = document.getField(fieldName);
			
			if (field != null) {
				// Only copy over if is not a no-value field (no-values are added just to be able to search for 'Other'-matches)
				if (!isNoValueField(attribute, field)) {
					final Object obj = getObjectValueFromField(attribute, field);
					
					final ItemAttributeValue<?> attributeValue = attribute.getValueFromObject(obj);
					
					if (attributeValue == null) {
						throw new IllegalStateException("attributeValue == null");
					}
	
					values.add(attributeValue);
				}
			}
		}
		
		return values;
	}
	
	
	@Override
	public void deleteItem(String itemId, Class<? extends Item> type) throws ItemIndexException {
		try {
			writer.deleteDocuments(new Term("id", itemId));
			writer.commit();
		} catch (IOException ex) {
			throw new ItemIndexException("Failed to delete document for item", ex);
		}
	}

	private IndexReader refreshReader() throws ItemIndexException {
		try {
			final DirectoryReader newReader = DirectoryReader.openIfChanged(this.reader, this.writer);

			if (newReader != null && this.reader != newReader) {
				this.reader.close();
				this.reader = newReader;
			}

//			if (!reader.isCurrent()) {
//				throw new IllegalStateException("Not current");
//			}
		} catch (IOException ex) {
			throw new ItemIndexException("Could not reopen reader", ex);
		}

		return reader;
	}
	
	private Document refreshReaderGetDoc(String itemId) throws ItemIndexException {
		final IndexReader reader = refreshReader();
		final IndexSearcher searcher = new IndexSearcher(reader);
		
		final Query query = new TermQuery(new Term("id", itemId));
		
		try {
			final TopDocs docs = searcher.search(query, Integer.MAX_VALUE);
			
			if (docs.totalHits > 1L) {
				throw new IllegalStateException("More than one match");
			}

			return searcher.doc(docs.scoreDocs[0].doc);
		} catch (IOException ex) {
			throw new ItemIndexException("Could not read doc with id " + itemId, ex);
		}
	}
	
	private static byte [] longsToBytes(Long [] longs) {
		// Encode longs as byte array
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(longs.length * 8);
		final DataOutputStream dataOutput = new DataOutputStream(baos);
		
		for (long l : longs) {
			try {
				dataOutput.writeLong(l);
			} catch (IOException ex) {
				throw new IllegalStateException("Exception while writing to buf", ex);
			}
		}
		
		return baos.toByteArray();
	}
	
	
	private static Long [] bytesToLongs(byte [] bytes) {
		// Encode longs as byte array
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final DataInputStream dataInput = new DataInputStream(bais);
		
		final List<Long> longs = new ArrayList<>(bytes.length / 8);
		
		for (;;) {
			try {
				final long l = dataInput.readLong();
				
				longs.add(l);
			} catch (EOFException ex) {
				break;
			}
			catch (IOException ex) {
				throw new IllegalStateException("Exception while reading from buf", ex);
			}
		}
		
		return longs.toArray(new Long[longs.size()]);
	}
	
	private static long encodeSize(int width, int height) {
		return ((long)width) << 32 | height;
	}

	@Override
	public synchronized void indexThumbnailSize(String itemId, Class<? extends Item> type, int index, int thumbWidth, int thumbHeight) throws ItemIndexException {
		final Document doc = refreshReaderGetDoc(itemId);
		
		// Get all value
		final IndexableField field = doc.getField(THUMBS_FIELD);

		final Long [] sizes = updateThumbnailSizeArray(field, index, thumbWidth, thumbHeight, 0L,
				f -> bytesToLongs(f.binaryValue().bytes),
				length -> new Long[length],
				(tw, th) -> encodeSize(tw, th));

		updateThumbnailSizes(doc, itemId, type, sizes);
	}
	
	private void updateThumbnailSizes(Document doc, String itemId, Class<? extends Item> type, Long [] sizes) throws ItemIndexException {
		final byte [] bytes = longsToBytes(sizes);

		
		// Workaround since does not work to search on id if just re-indexing field
		// and the field contains eg. hyphen
		// TODO might be necessary for other fields as well with regard to facets
		/*
		final IndexableField idField = doc.getField("id");

		doc.removeField("id");
		doc.add(new StringField("id", idField.stringValue(), Field.Store.YES));
		*/

		// Seems like update resets string fields to TextField etc so matching does not work,
		// just recreate document from attributes
		final Set<ItemAttribute> attributes = itemTypes.getTypeInfo(type).getAttributes().asSet();
		
		final List<ItemAttributeValue<?>> values = getValuesFromDocument(doc, attributes);

		final Document newDoc = new Document();
		
		// Get user id from existing doc
		final String userId = doc.getField("userId").stringValue();
		
		addToDocument(newDoc, itemTypes.getTypeInfo(type), userId, values);
		
		newDoc.add(new StoredField(THUMBS_FIELD, bytes));

		try {
			writer.updateDocument(new Term("id", itemId), newDoc);
			writer.commit();
		} catch (IOException ex) {
			throw new ItemIndexException("Failed to update thumb sizes", ex);
		}
	}
	

	@Override
	public void deletePhotoAndThumbnailForItem(String itemId, Class<? extends Item> type, int photoNo) throws ItemIndexException {
		final Document doc = refreshReaderGetDoc(itemId);
		final IndexableField field = doc.getField(THUMBS_FIELD);
		final Long [] sizes = bytesToLongs(field.binaryValue().bytes);

		final Long [] updated = deleteThumbnail(sizes, photoNo, length -> new Long[length]);

		updateThumbnailSizes(doc, itemId, type, updated);
	}

	@Override
	public void movePhotoAndThumbnailForItem(String itemId, Class<? extends Item> type, int photoNo, int toIndex) throws ItemIndexException {
		final Document doc = refreshReaderGetDoc(itemId);
		final IndexableField field = doc.getField(THUMBS_FIELD);
		final Long [] sizes = bytesToLongs(field.binaryValue().bytes);

		final Long toMove = sizes[photoNo];
		
		final Long [] moved = moveThumbnail(sizes, toMove, photoNo, toIndex, length -> new Long[length]);

		updateThumbnailSizes(doc, itemId, type, moved);
	}

	@Override
	public IndexSearchCursor search(
			List<Class<? extends Item>> types,
			String freeText,
			List<Criterium> criteria,
			List<SortAttributeAndOrder> sortOrder,
			boolean returnSortAttributeValues,
			Set<ItemAttribute> fieldAttributes,
			Set<ItemAttribute> facetAttributes) throws ItemIndexException {
		
		refreshReader();

		final IndexSearcher searcher = new IndexSearcher(reader);
		
		final Query query;
		
		final String trimmedFreetext = ItemIndex.trimAndLowercaseFreetext(freeText);

		if (types == null) {
			throw new IllegalArgumentException("types == null");
		}
		else if (types.isEmpty() && trimmedFreetext == null) {
			// Optimization, no types specified so just return no docs since
			// does not have any matches no matter what criterium is passed in
			query = new MatchNoDocsQuery();
		}
		else{
			if ((criteria != null && !criteria.isEmpty()) || trimmedFreetext != null) {
				
				// Criteria or freetext
				
				final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
	
				final List<Query> appendQueries = new ArrayList<>();
				
				if (criteria != null && !criteria.isEmpty()) {
					final Query criteriaQuery = createQueryFromCriteria(criteria, queryBuilder);
					
					appendQueries.add(criteriaQuery);
				}
				
				if (trimmedFreetext != null) {
					// Make freetext query over all fields that are marked as freetext for all the types specified
					final Query freetextQuery = createFreetextQuery(types, trimmedFreetext, itemTypes);
					
					if (freetextQuery != null) {
						appendQueries.add(freetextQuery);
					}
				}
				
				// Must add criteria for types
				query = makeTypesQuery(types, Occur.MUST, appendQueries.toArray(new Query[appendQueries.size()])); // Occur.MUST for criteriaQuery
			}
			else {
				final Set<Class<? extends Item>> typesSet = new HashSet<>(types);
				final Set<Class<? extends Item>> allTypesSet = itemTypes.getAllTypesSet();

				if (typesSet.equals(allTypesSet)) {
					// All supported types included so can just call MatchAllDocsQuery()
					query = new MatchAllDocsQuery();
				}
				else {
					query = makeTypesQuery(typesSet);
				}
			}
		}

		final ResultsCollector resultsCollector;
		try {
			resultsCollector = new ResultsCollector();

			searcher.search(query, resultsCollector);
		} catch (IOException ex) {
			throw new ItemIndexException("Failed to search", ex);
		}
		
		
		final List<Document> documents = new ArrayList<>(resultsCollector.documents.size());
	
		for (int matchingDoc : resultsCollector.documents) {
			try {
				final Document document = reader.document(matchingDoc);

				documents.add(document);
			} catch (IOException ex) {
				throw new ItemIndexException("Failed to get document " + matchingDoc, ex);
			}
		}

		final ItemsFacets facets = facetAttributes != null
				? computeFacets(documents, facetAttributes, itemTypes)
				: null;
		
		return new IndexSearchCursor() {

			@Override
			public int getTotalMatchCount() {
				return documents.size();
			}

			@Override
			public List<String> getItemIDs(int initialIdx, int count) {
				
				Stream<Document> s = documents.stream()
//						.peek(d -> System.out.println("Got document " +  "/" + d.getField("id")))
						.skip(initialIdx)
						.limit(count);

				final Comparator<Document> comparator = SortUtils.makeSortItemsComparator(sortOrder, (doc, attribute) -> getObjectValueFromDocument(doc, attribute));

				if (comparator != null) {
					s = s.sorted(comparator);
				}
				
				return s
						.map(d -> d.getField("id").stringValue())
						.collect(Collectors.toList());
			}

			
			@Override
			public List<SearchItem> getItemIDsAndTitles(int initialIdx, int count) {
				
				Stream<Document> s = documents.stream()
						.skip(initialIdx)
						.limit(count);
				
				final Comparator<Document> comparator = SortUtils.makeSortItemsComparator(sortOrder, (doc, attribute) -> getObjectValueFromDocument(doc, attribute));

				if (comparator != null) {
					s = s.sorted(comparator);
				}
				

				return s.map(d -> {
							
							final Integer thumbWidth;
							final Integer thumbHeight;
							
							final IndexableField thumbField = d.getField(THUMBS_FIELD);
							if (thumbField != null) {
								final byte [] data = thumbField.binaryValue().bytes;
								final Long [] longs = bytesToLongs(data);
								
								if (longs.length > 0) {
									thumbWidth = (int)(longs[0] >> 32);
									thumbHeight = (int)(longs[0] & 0xFFFFFFFF); 
								}
								else {
									thumbWidth = null;
									thumbHeight = null;
								}
							}
							else {
								thumbWidth = null;
								thumbHeight = null;
							}
							
							final FieldValues<SortAttribute> sortValues;
							
							if (returnSortAttributeValues && sortOrder != null && !sortOrder.isEmpty()) {
								sortValues = new FieldValues<SortAttribute>() {
									@Override
									public Object getValue(SortAttribute attribute) {
										
										final IndexableField field = d.getField(ItemIndex.fieldName(attribute));
										
										return field != null && !isNoValueField(attribute, field)
												? getObjectValueFromField(attribute, field)
												: null;
									}
								};
							}
							else {
								sortValues = null;
							}
							
							final FieldValues<ItemAttribute> fieldValues;
							
							if (fieldAttributes != null && !fieldAttributes.isEmpty()) {
								fieldValues = new FieldValues<ItemAttribute>() {
									@Override
									public Object getValue(ItemAttribute attribute) {
										
										final IndexableField field = d.getField(ItemIndex.fieldName(attribute));
										
										return field != null && !isNoValueField(attribute, field)
												? getObjectValueFromField(attribute, field)
											: null;
									}
								};
							}
							else {
								fieldValues = null;
							}
							

							// TODO perhaps parameterize attribute names
							// since this layer ought to be more generic
							final IndexableField titleField = d.getField("title");

							return new IndexSearchItem(
									d.getField("id").stringValue(),
									titleField != null ? titleField.stringValue() : null,
									thumbWidth,
									thumbHeight,
									sortValues,
									fieldValues
							);
						})
						.collect(Collectors.toList());
			}

			@Override
			public ItemsFacets getFacets() {
				
				// Must find all distinct results of each attribute for the items
				return facets;
			}
		};
	}

	private static Query makeTypesQuery(Collection<Class<? extends Item>> types) {
		return makeTypesQuery(types, null, null);
	}

	private static Query makeTypesQuery(Collection<Class<? extends Item>> types, Occur appendOccur, Query [] appendQuery) {

		if (types == null) {
			throw new IllegalArgumentException("types == null");
		}
		
		if (types.isEmpty()) {
			throw new IllegalArgumentException("no types");
		}

		final Query query;
		
		if (types.size() == 1 && appendQuery == null) {
			// Can return a simple term query since only checking on one type
			query = createTypeTermQuery(types.iterator().next());
		}
		else {
			// Must use a boolean query to "should" together types
			final BooleanQuery.Builder typesQueryBuilder = new BooleanQuery.Builder();

			for (Class<? extends Item> type : types) {
				final Query oneTypeQuery = createTypeTermQuery(type);
				typesQueryBuilder.add(oneTypeQuery, Occur.SHOULD);
			}

			if (appendQuery == null || appendQuery.length == 0) {
				query = typesQueryBuilder.build();
			}
			else {
				// Append query, either should or must
				for (Query aq : appendQuery) {
					typesQueryBuilder.add(aq, appendOccur);
				}
				
				query = typesQueryBuilder.build();
			}
		}

		return query;
	}

	private static TermQuery createTypeTermQuery(Class<? extends Item> type) {
		return new TermQuery(new Term(TYPE_FIELD, ItemTypes.getTypeName(type)));
	}
	
	
	private static class QueryAndOccur {
		private final Query query;
		private final Occur occur;
		
		public QueryAndOccur(Query query, Occur occur) {
			this.query = query;
			this.occur = occur;
		}
	}
	
	private static Query createFreetextQuery(Collection<Class<? extends Item>> types, String freeText, ItemTypes itemTypes) {
		
		// Get SortAttribute since this has equals() and hashCode() on base class,
		// this makes us find distinct attributes
		final Set<DistinctAttribute> distinctAttributes = itemTypes.getFreetextAttributes(types);
		
		final Query query;
		
		if (distinctAttributes.isEmpty()) {
			query = null;
		}
		else if (distinctAttributes.size() == 1) {
			final String fieldName = ItemIndex.freetextFieldName(distinctAttributes.iterator().next());

			query = new TermQuery(new Term(fieldName, freeText));
		}
		else {
		
			final BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
		
			for (DistinctAttribute attribute : distinctAttributes) {
				final String fieldName = ItemIndex.freetextFieldName(attribute);
				
				final TermQuery termQuery = new TermQuery(new Term(fieldName, freeText));
				
				booleanQuery.add(termQuery, Occur.SHOULD);
			}
			
			query = booleanQuery.build();
		}
		
		return query;
	}

	private static Query createQueryFromCriteria(List<Criterium> criteria, BooleanQuery.Builder queryBuilder) {
		for (Criterium criterium : criteria) {

			final ItemAttribute attribute = criterium.getAttribute();
			final String fieldName = ItemIndex.fieldName(attribute);

			final QueryAndOccur queryAndOccur;
			
			if (criterium instanceof ComparisonCriterium<?>) {
				queryAndOccur = createComparisonQuery(criterium, fieldName);
			}
			else if (criterium instanceof InCriterium<?>) {
				queryAndOccur = createInQuery(criterium, fieldName);
			}
			else if (criterium instanceof RangesCriterium<?, ?>) {
				queryAndOccur = createRangeQuery(criterium, fieldName);
			}
			else if (criterium instanceof NoValueCriterium) {
				queryAndOccur = createNoValueQueryAndOccur(criterium, fieldName);
			}
			else {
				throw new UnsupportedOperationException("Unknown criterium");
			}
			
			queryBuilder.add(queryAndOccur.query, queryAndOccur.occur);
		}
		

		final Query query =  queryBuilder.build();

		return query;
	}
	
	private static QueryAndOccur createComparisonQuery(Criterium criterium, String fieldName) {
		final Query query;
		final Occur occur;

		final ComparisonOperator comparisonOperator = ((ComparisonCriterium<?>) criterium).getComparisonOperator();
		
		if (criterium instanceof StringCriterium) {
			final String value = ((StringCriterium)criterium).getValue();
			
			query = new TermQuery(new Term(fieldName, value));
			occur = Occur.MUST;
		}
		else if (criterium instanceof IntegerCriterium) {
			final Integer value = ((IntegerCriterium)criterium).getValue();
			
			switch (comparisonOperator) {
			case EQUALS:
				query = IntPoint.newExactQuery(fieldName, value);
				occur = Occur.MUST;
				break;
				
			case NOT_EQUALS:
				query = IntPoint.newExactQuery(fieldName, value);
				occur = Occur.MUST_NOT;
				break;
			
			case LESS_THAN:
				final BooleanQuery.Builder lessThanQuery = new BooleanQuery.Builder();
				
				lessThanQuery
					.add(IntPoint.newRangeQuery(fieldName, Integer.MIN_VALUE, value), Occur.MUST)
					.add(IntPoint.newExactQuery(fieldName, value), Occur.MUST_NOT);
				
				query = lessThanQuery.build();
				occur = Occur.MUST;
				break;
				
			case LESS_THAN_OR_EQUALS:
				query = IntPoint.newRangeQuery(fieldName, Integer.MIN_VALUE, value);
				occur = Occur.MUST;
				break;
				
			case GREATER_THAN:
				final BooleanQuery.Builder greaterThanQuery = new BooleanQuery.Builder();
				
				greaterThanQuery
					.add(IntPoint.newRangeQuery(fieldName, value, Integer.MAX_VALUE), Occur.MUST)
					.add(IntPoint.newExactQuery(fieldName, value), Occur.MUST_NOT);
				
				query = greaterThanQuery.build();
				occur = Occur.MUST;
				break;
			
			case GREATER_THAN_OR_EQUALS:
				query = IntPoint.newRangeQuery(fieldName, value, Integer.MAX_VALUE);
				occur = Occur.MUST;
				break;
				
			default:
				throw new UnsupportedOperationException("Unknown comparison operator: " + comparisonOperator);
			}
		}
		else if (criterium instanceof DecimalCriterium) {
			final BigDecimal value = ((DecimalCriterium)criterium).getValue();
			
			switch (comparisonOperator) {
			case EQUALS:
				query = DoublePoint.newExactQuery(fieldName, value.doubleValue());
				occur = Occur.MUST;
				break;
				
			case NOT_EQUALS:
				query = DoublePoint.newExactQuery(fieldName, value.doubleValue());
				occur = Occur.MUST_NOT;
				break;
			
			case LESS_THAN:
				final BooleanQuery.Builder lessThanQuery = new BooleanQuery.Builder();
				
				lessThanQuery
					.add(DoublePoint.newRangeQuery(fieldName, Double.MIN_VALUE, value.doubleValue()), Occur.MUST)
					.add(DoublePoint.newExactQuery(fieldName, value.doubleValue()), Occur.MUST_NOT);
				
				query = lessThanQuery.build();
				occur = Occur.MUST;
				break;
				
			case LESS_THAN_OR_EQUALS:
				query = DoublePoint.newRangeQuery(fieldName, Double.MIN_VALUE, value.doubleValue());
				occur = Occur.MUST;
				break;
				
			case GREATER_THAN:
				final BooleanQuery.Builder greaterThanQuery = new BooleanQuery.Builder();
				
				greaterThanQuery
					.add(DoublePoint.newRangeQuery(fieldName, value.doubleValue(), Double.MAX_VALUE), Occur.MUST)
					.add(DoublePoint.newExactQuery(fieldName, value.doubleValue()), Occur.MUST_NOT);
				
				query = greaterThanQuery.build();
				occur = Occur.MUST;
				break;
			
			case GREATER_THAN_OR_EQUALS:
				query = DoublePoint.newRangeQuery(fieldName, value.doubleValue(), Double.MAX_VALUE);
				occur = Occur.MUST;
				break;
				
			default:
				throw new UnsupportedOperationException("Unknown comparison operator: " + comparisonOperator);
			}
		}
		else {
			throw new UnsupportedOperationException("Unknown criterium");
		}

		return new QueryAndOccur(query, occur);
	}

	private static QueryAndOccur createInQuery(Criterium criterium, String fieldName) {
		final Query query;
		final Occur occur;
		
		final InCriterium<?> inCriterium = (InCriterium<?>)criterium;
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final List<InCriteriumValue<?>> values = (List)inCriterium.getValues();
		
		final AttributeType attributeType = criterium.getAttribute().getAttributeType();

		
		if (values.size() == 1 && !inCriterium.includeItemsWithNoValue() && (values.get(0).getSubCritera() == null || values.get(0).getSubCritera().isEmpty())) {
			// If single value and not including no-value attributes and no subattributes, creare
			query = createExactQueryForInCriteriumValue(fieldName, attributeType, values.get(0));
		}
		else {

			final BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

			for (InCriteriumValue<?> value : values) {
				booleanQuery.add(createExactQueryForInCriteriumValue(fieldName, attributeType, value), Occur.SHOULD);
			}
			
			if (inCriterium.includeItemsWithNoValue()) {
				booleanQuery.add(createNoValueQueryOnly(inCriterium, fieldName), Occur.SHOULD);
			}

			query = booleanQuery.build();
		}
		
		occur = Occur.MUST;
		
		return new QueryAndOccur(query, occur);
	}

	private static Query createExactQueryForInCriteriumValue(String fieldName, AttributeType attributeType, InCriteriumValue<?> value) {
		
		final Query query;

		if (value.getSubCritera() != null) {
			// Sub-criteria so must have boolean query with must for each
			final BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
			
			final Query valueQuery = createExactQueryForValue(fieldName, attributeType, value.getValue());
			booleanQuery.add(valueQuery, Occur.MUST);
			
			// Must match all the sub queries that are themselves in-queries
			for (Criterium sub : value.getSubCritera()) {
				
				final Query subQuery;
				final String subFieldName = ItemIndex.fieldName(sub.getAttribute());

				if (sub instanceof InCriterium<?>) {
					subQuery = createInQuery(sub, subFieldName).query;
				}
				else if (sub instanceof NoValueCriterium) {
					subQuery = createNoValueQueryOnly(sub, subFieldName);
				}
				else {
					throw new UnsupportedOperationException("Unknown subceriteria: " + sub.getClass());
				}

				booleanQuery.add(subQuery, Occur.MUST);
			}
			
			query = booleanQuery.build();
		}
		else {
			query = createExactQueryForValue(fieldName, attributeType, value.getValue());
		}

		return query;
	}
	
	private static Query createExactQueryForValue(String fieldName, AttributeType attributeType, Object value) {
		final Query query;
		
		switch (attributeType) {
		case STRING:
			query = new TermQuery(new Term(fieldName, (String)value));
			break;
			
		case INTEGER:
			query = IntPoint.newExactQuery(fieldName, (Integer)value);
			break;
			
		case DECIMAL:
			query = DoublePoint.newExactQuery(fieldName, ((BigDecimal)value).doubleValue());
			break;
			
		case ENUM:
			query = new TermQuery(new Term(fieldName, ((Enum<?>)value).name()));
			break;
			
			
		default:
			throw new UnsupportedOperationException("Unknown attribute type: " + attributeType);
		}

		return query;
	}

	private static QueryAndOccur createNoValueQueryAndOccur(Criterium criterium, String fieldName) {
		final Occur occur = Occur.MUST;
		
		final Query query = createNoValueQueryOnly(criterium, fieldName);
		
		return new QueryAndOccur(query, occur);
	}

	private static Query createNoValueQueryOnly(Criterium criterium, String fieldName) {

		final AttributeType attributeType = criterium.getAttribute().getAttributeType();
		
		final Query query;
		
		switch (attributeType) {
		case STRING:
			query = new TermQuery(new Term(fieldName, STRING_NONE));
			break;
			
		case INTEGER:
			query = IntPoint.newExactQuery(fieldName, INTEGER_NONE);
			break;
			
		case LONG:
			query = LongPoint.newExactQuery(fieldName, LONG_NONE);
			break;
			
		case DECIMAL:
			query = DoublePoint.newExactQuery(fieldName, DOUBLE_NONE);
			break;
			
		case BOOLEAN:
			query = IntPoint.newExactQuery(fieldName, BOOLEAN_NONE);
			break;
			
		case ENUM:
			query = new TermQuery(new Term(fieldName, ENUM_NONE));
			break;

		case DATE:
			query = LongPoint.newExactQuery(fieldName, DATE_NONE);
			break;
			
		default:
			throw new UnsupportedOperationException("Unknown attribute type " + attributeType);
		}

		return query;
	}

	private static QueryAndOccur createRangeQuery(Criterium criterium, String fieldName) {
		final Query query;
		final Occur occur;
		
		if (criterium instanceof IntegerRangesCriterium) {
			final IntegerRangesCriterium integerRangeCriterium = (IntegerRangesCriterium)criterium;

			query = createRangesQuery(integerRangeCriterium, (f, r) -> createIntegerRangeQuery(f, r));
			
			occur = Occur.MUST;
		}
		else if (criterium instanceof DecimalRangesCriterium) {
			final DecimalRangesCriterium decimalRangeCriterium = (DecimalRangesCriterium)criterium;
			
			query = createRangesQuery(decimalRangeCriterium, (f, r) -> createDecimalRangeQuery(f, r));

			occur = Occur.MUST;
		}
		else {
			throw new UnsupportedOperationException("Reange query");
		}
		

		return new QueryAndOccur(query, occur);
	}

	private static <T extends Comparable<T>, RANGE extends Range<T>, CRITERIUM extends RangesCriterium<T, RANGE>>
		Query createRangesQuery(CRITERIUM criterium, BiFunction<String, RANGE, Query> createQueryForOneRange) {
		
		final Query query;
		final String fieldName = criterium.getAttribute().getName();

		final RANGE [] ranges = criterium.getRanges();
		
		if (ranges.length == 1 && !criterium.includeItemsWithNoValue()) {
			query = createQueryForOneRange.apply(fieldName, ranges[0]);
		}
		else {
			// Nest in should-query
			final BooleanQuery.Builder rangeBooleanQuery = new BooleanQuery.Builder();
            
			for (RANGE range : ranges) {
				rangeBooleanQuery.add(createQueryForOneRange.apply(fieldName, range), Occur.SHOULD);
			}

			if (criterium.includeItemsWithNoValue()) {
				// Add a no-match query
				rangeBooleanQuery.add(createNoValueQueryOnly(criterium, fieldName), Occur.SHOULD);
			}

			query = rangeBooleanQuery.build();
		}

		return query;
	}

	private static Query createIntegerRangeQuery(String fieldName, IntegerRange range) {

		final Query rangeQuery;

		final int lowerValue;
		final int upperValue;
		final boolean includeLower;
		final boolean includeUpper;
		
		if (range.getLowerValue() == null) {
			lowerValue = 0;
			includeLower = true;
		}
		else {
			lowerValue = range.getLowerValue();
			includeLower = range.includeLower();
		}
		
		if (range.getUpperValue() == null) {
			upperValue = Integer.MAX_VALUE;
			includeUpper = true;
		}
		else {
			upperValue = range.getUpperValue();
			includeUpper = range.includeUpper();
		}
		
		if (includeLower && includeUpper) {

			rangeQuery = IntPoint.newRangeQuery(fieldName, lowerValue, upperValue);
			
		}
		else if (includeLower) {
			
			rangeQuery = IntPoint.newRangeQuery(fieldName, lowerValue, upperValue - 1);

		}
		else if (includeUpper) {

			rangeQuery = IntPoint.newRangeQuery(fieldName, lowerValue + 1, upperValue);

		}
		else {
			rangeQuery = IntPoint.newRangeQuery(fieldName, lowerValue + 1, upperValue - 1);
		
		}

		return rangeQuery;
	}
	
	private static Query createDecimalRangeQuery(String fieldName, DecimalRange range) {
		final Query rangeQuery;

		final BigDecimal lowerValue;
		final BigDecimal upperValue;
		final boolean includeLower;
		final boolean includeUpper;
		
		if (range.getLowerValue() == null) {
			lowerValue = BigDecimal.ZERO;
			includeLower = true;
		}
		else {
			lowerValue = range.getLowerValue();
			includeLower = range.includeLower();
		}
		
		if (range.getUpperValue() == null) {
			// TODO better solution? Just set a large bigdecimal value
			upperValue = new BigDecimal("99999999999999999999999");
			includeUpper = true;
		}
		else {
			upperValue = range.getUpperValue();
			includeUpper = range.includeUpper();
		}
		
		if (includeLower && includeUpper) {
			rangeQuery = DoublePoint.newRangeQuery(
					fieldName,
					lowerValue.doubleValue(),
					upperValue.doubleValue());
		}
		else if (includeLower) {
			rangeQuery = new BooleanQuery.Builder()
					.add(DoublePoint.newRangeQuery(
						fieldName,
						lowerValue.doubleValue(),
						upperValue.doubleValue()), Occur.MUST)
					.add(DoublePoint.newExactQuery(fieldName, upperValue.doubleValue()), Occur.MUST_NOT)
					.build();
		}
		else if (includeUpper) {
			rangeQuery = new BooleanQuery.Builder()
					.add(DoublePoint.newRangeQuery(
						fieldName,
						lowerValue.doubleValue(),
						upperValue.doubleValue()), Occur.MUST)
					.add(DoublePoint.newExactQuery(fieldName, lowerValue.doubleValue()), Occur.MUST_NOT)
					.build();
			
		}
		else {
			rangeQuery = new BooleanQuery.Builder()
					.add(DoublePoint.newRangeQuery(
						fieldName,
						lowerValue.doubleValue(),
						upperValue.doubleValue()), Occur.MUST)
					.add(DoublePoint.newExactQuery(fieldName, lowerValue.doubleValue()), Occur.MUST_NOT)
					.add(DoublePoint.newExactQuery(fieldName, upperValue.doubleValue()), Occur.MUST_NOT)
					.build();
			
		}

		return rangeQuery;
	}

	private static Integer getIntegerValueFromField(IndexableField field) {
		return field.numericValue().intValue();
	}

	private static Long getLongValueFromField(IndexableField field) {
		return field.numericValue().longValue();
	}

	private static BigDecimal getDecimalValueFromField(IndexableField field) {
		return BigDecimal.valueOf(field.numericValue().doubleValue());
	}

	private static <T extends Enum<T>> T getEnumValueFromField(Class<T> enumClass, IndexableField field) {
		final T [] enums = enumClass.getEnumConstants();

		T found = null;

		for (T e : enums) {
			if (e.name().equals(field.stringValue())) {
				found = e;
				break;
			}
		}

		if (found == null) {
			throw new IllegalStateException("No enum found for " + field.stringValue() + " in enum " + enumClass.getName());
		}

		return found;
	}

	private static Boolean getBooleanValueFromField(IndexableField field) {
		return field.numericValue().intValue() != 0 ? true : false;
	}

	private static Date getDateValueFromField(IndexableField field) {
		final long value = field.numericValue().longValue();
		
		return value != LONG_NONE ? new Date(value) : null;
	}

	private static OffsetDateTime getTZDateValueFromField(IndexableField field) {
		final String value = field.stringValue();
		
		return value != null && !STRING_NONE.equals(value) ? OffsetDateTime.parse(value, TZDATE_FORMATTER) : null;
	}

	private static Comparable<?> getObjectValueFromDocument(Document document, PropertyAttribute attribute) {
	
		final String fieldName = ItemIndex.fieldName(attribute);
		final IndexableField field = document.getField(fieldName);

		final Comparable<?> result = field != null ? getObjectValueFromField(attribute, field) : null;

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Comparable<?> getObjectValueFromField(PropertyAttribute attribute, IndexableField field) {
		final AttributeType attributeType = attribute.getAttributeType();
		
		final Comparable<?> result;
		
		switch (attributeType) {
		case STRING:
			result = field.stringValue();
			break;
			
		case INTEGER:
			result = getIntegerValueFromField(field);
			break;
			
		case LONG:
			result = getLongValueFromField(field);
			break;
			
		case DECIMAL:
			result = getDecimalValueFromField(field);
			break;
			
		case ENUM:
			result = getEnumValueFromField((Class<? extends Enum>)attribute.getAttributeValueClass(), field);
			break;
			
		case BOOLEAN:
			result = getBooleanValueFromField(field);
			break;
			
		case DATE:
			result = getDateValueFromField(field);
			break;
			
		case TZDATE:
			result = getTZDateValueFromField(field);
			break;
			
		default:
			throw new UnsupportedOperationException("Unknown attribute type " + attributeType);
		}
		
		return result;
	}
	
	private static boolean isNoValueField(PropertyAttribute attribute, IndexableField field) {
		final AttributeType attributeType = attribute.getAttributeType();
		
		final boolean result;
		
		switch (attributeType) {
		case STRING:
			result = STRING_NONE.equals(field.stringValue());
			break;
			
		case INTEGER:
			result = INTEGER_NONE == getIntegerValueFromField(field);
			break;
			
		case LONG:
			result = LONG_NONE == getLongValueFromField(field);
			break;
			
		case DECIMAL:
			result = DOUBLE_NONE == field.numericValue().doubleValue();
			break;
			
		case ENUM:
			result = ENUM_NONE.equals(field.stringValue());
			break;
			
		case BOOLEAN:
			result = BOOLEAN_NONE == field.numericValue().intValue();
			break;
			
		case DATE:
			result = null == getDateValueFromField(field);
			break;
			
		case TZDATE:
			result = null == getTZDateValueFromField(field);
			break;
			
		default:
			throw new UnsupportedOperationException("Unknown attribute type " + attributeType);
		}
		
		return result;
	}

	
	private static ItemsFacets computeFacets(List<Document> documents, Set<ItemAttribute> facetedAttributes, ItemTypes itemTypes) {
		return FacetUtils.computeFacets(documents, facetedAttributes, itemTypes, new FacetUtils.FacetFunctions<Document, IndexableField>() {
			@Override
			public boolean isType(Document d, String typeName) {
				return d.getField(TYPE_FIELD).stringValue().equals(typeName);
			}

			@Override
			public IndexableField getField(Document item, String fieldName) {
				return item.getField(fieldName);
			}

			@Override
			public Integer getIntegerValue(IndexableField field) {
				return getIntegerValueFromField(field);
			}

			@Override
			public BigDecimal getDecimalValue(IndexableField field) {
				return getDecimalValueFromField(field);
			}

			@Override
			public <T extends Enum<T>> T getEnumValue(Class<T> enumClass, IndexableField field) {
				return getEnumValueFromField(enumClass, field);
			}

			@Override
			public Boolean getBooleanValue(IndexableField field) {
				return getBooleanValueFromField(field);
			}

			@Override
			public Object getObjectValue(ItemAttribute attribute, IndexableField field) {
				return getObjectValueFromField(attribute, field);
			}

			@Override
			public boolean isNoValue(ItemAttribute attribute, IndexableField field) {
				return isNoValueField(attribute, field);
			}
		});
	}
	
	
	private static class ResultsCollector implements Collector {
		private final Set<Integer> documents;
		
		ResultsCollector() {
			this.documents = new HashSet<>();
		}
		
		@Override
		public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
			
			final int docBase = context.docBase;
			
			return new LeafCollector() {
				
				@Override
				public void setScorer(Scorer scorer) throws IOException {
					
				}
				
				@Override
				public void collect(int docId) throws IOException {
					documents.add(docBase + docId);
				}
			};
		}

		@Override
		public boolean needsScores() {
			return false;
		}
	}
	
	
	@Override
	public ItemId[] expandToItemIdUserId(String[] itemIds) throws ItemIndexException {
		
		if (itemIds == null) {
			throw new IllegalArgumentException("itemIds == null");
		}

		if (itemIds.length == 0) {
			throw new IllegalArgumentException("No item IDs specified");
		}

		final Map<String, Integer> itemIdToIndex = new HashMap<>(itemIds.length);

		for (int i = 0; i < itemIds.length; ++ i) {
			itemIdToIndex.put(itemIds[i], i);
		}
		
		final ItemId[] result = new ItemId[itemIds.length];
		
		// Query all documents for item IDs
		final BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

		for (String value : itemIds) {
			if (value == null || value.trim().isEmpty()) {
				throw new IllegalArgumentException("null or empty itemId");
			}
			
			booleanQuery.add(new TermQuery(new Term("id", value)), Occur.SHOULD);
		}

		final Query query = booleanQuery.build();
		final TopDocs topDocs;
		try {
			topDocs = new IndexSearcher(refreshReader()).search(query, itemIds.length);
		} catch (IOException ex) {
			throw new ItemIndexException("Failed to search for items", ex);
		}
		
		if (topDocs.totalHits != itemIds.length) {
			throw new IllegalStateException("Mismatch in found and input of item IDs: " + topDocs.totalHits + " / " + itemIds.length);
		}
		
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			try {
				final Document doc = reader.document(scoreDoc.doc);
				
				final String id = doc.getField("id").stringValue();
				final String userId = doc.getField("userId").stringValue();
				
				final int index = itemIdToIndex.get(id);
				
				result[index] = new ItemId(userId, id);
				
			} catch (IOException ex) {
				throw new ItemIndexException("Failed to read doc", ex);
			}
		}
		
		for (int i = 0; i < result.length; ++ i) {
			if (result[i] == null) {
				throw new IllegalStateException("Did not find use ID for all item IDs: " + i);
			}
		}
		
		return result;
	}

	@Override
	public void close() throws Exception {
		try {
			writer.close();
		}
		finally {
			try {
				reader.close();
			}
			finally {
				directory.close();
			}
		}
	}
}
