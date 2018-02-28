package com.test.cv.index.lucene;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.test.cv.index.IndexSearchCursor;
import com.test.cv.index.ItemIndex;
import com.test.cv.index.ItemIndexException;
import com.test.cv.model.DecimalAttributeValue;
import com.test.cv.model.IntegerAttributeValue;
import com.test.cv.model.Item;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.ItemAttributeValue;
import com.test.cv.model.StringAttributeValue;
import com.test.cv.search.criteria.ComparisonOperator;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.criteria.DecimalCriterium;
import com.test.cv.search.criteria.DecimalRangeCriterium;
import com.test.cv.search.criteria.IntegerCriterium;
import com.test.cv.search.criteria.IntegerRangeCriterium;
import com.test.cv.search.criteria.RangeCriteria;
import com.test.cv.search.criteria.SingleValueCriteria;
import com.test.cv.search.criteria.StringCriterium;
import com.test.cv.search.facets.ItemsFacets;

public class LuceneItemIndex implements ItemIndex {

	private final Directory directory;
	private IndexWriter writer;
	private DirectoryReader reader;
	
	public LuceneItemIndex(String indexPath) throws IOException {
		this(FSDirectory.open(new File(indexPath).toPath()));
	}

	public LuceneItemIndex(Directory directory) throws IOException {

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
	public void indexItemAttributes(Class<? extends Item> itemType, List<ItemAttributeValue<?>> attributeValues) throws ItemIndexException {

		final Document document = new Document();

		for (ItemAttributeValue<?> attributeValue : attributeValues) {
			
			final Field field;
			
			final String fieldName = attributeValue.getAttribute().getName();

			if (attributeValue instanceof StringAttributeValue) {
				final String value = ((StringAttributeValue)attributeValue).getValue();
				
				field = new StringField(fieldName, value, Field.Store.NO);
			}
			else if (attributeValue instanceof IntegerAttributeValue) {
				field = new IntPoint(fieldName, ((IntegerAttributeValue)attributeValue).getValue());
			}
			else if (attributeValue instanceof DecimalAttributeValue) {
				final double value = ((DecimalAttributeValue)attributeValue).getValue().doubleValue();
				// TODO Lucene does not support decimals yet
				field = new DoublePoint(fieldName, value);
			}
			else {
				throw new UnsupportedOperationException("Unknown attribute type : " + attributeValue.getClass());
			}
			
			document.add(field);
		}

		try {
			/*
			final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
			final IndexWriter writer = new IndexWriter(directory, config);
			*/
			writer.addDocument(document);
			writer.commit();
			//writer.close();
			
		} catch (IOException ex) {
			throw new ItemIndexException("Failed to write document for item", ex);
		}
	}

	@Override
	public IndexSearchCursor search(String freeText, List<Criterium> criteria, List<ItemAttribute> facetAttributes) throws ItemIndexException {

		try {
			final DirectoryReader newReader = DirectoryReader.openIfChanged(this.reader);

			if (newReader != null) {
				this.reader = newReader;
			}
		} catch (IOException ex) {
			throw new ItemIndexException("Could not reopen reader", ex);
		}

		final IndexSearcher searcher = new IndexSearcher(reader);
		
		final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

		for (Criterium criterium : criteria) {

			final ItemAttribute attribute = criterium.getAttribute();
			final String fieldName = attribute.getName();

			final Query query;
			
			final Occur occur;

			if (criterium instanceof SingleValueCriteria<?>) {
				
				final ComparisonOperator comparisonOperator = ((SingleValueCriteria<?>) criterium).getComparisonOperator();
				
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
			}
			else if (criterium instanceof RangeCriteria<?>) {
				
				if (criterium instanceof IntegerRangeCriterium) {
					final IntegerRangeCriterium integerRangeCriterium = (IntegerRangeCriterium)criterium;
					
					if (integerRangeCriterium.includeLower() && integerRangeCriterium.includeUpper()) {

						query = IntPoint.newRangeQuery(fieldName, integerRangeCriterium.getLowerValue(), integerRangeCriterium.getUpperValue());
						
					}
					else if (integerRangeCriterium.includeLower()) {
						
						query = IntPoint.newRangeQuery(fieldName, integerRangeCriterium.getLowerValue(), integerRangeCriterium.getUpperValue() - 1);

					}
					else if (integerRangeCriterium.includeUpper()) {

						query = IntPoint.newRangeQuery(fieldName, integerRangeCriterium.getLowerValue() + 1, integerRangeCriterium.getUpperValue());

					}
					else {

						query = IntPoint.newRangeQuery(fieldName, integerRangeCriterium.getLowerValue() + 1, integerRangeCriterium.getUpperValue() - 1);
					
					}
					
					occur = Occur.MUST;
				}
				else if (criterium instanceof DecimalRangeCriterium) {
					final DecimalRangeCriterium decimalRangeCriterium = (DecimalRangeCriterium)criterium;

					if (decimalRangeCriterium.includeLower() && decimalRangeCriterium.includeUpper()) {
						query = DoublePoint.newRangeQuery(
								fieldName,
								decimalRangeCriterium.getLowerValue().doubleValue(),
								decimalRangeCriterium.getUpperValue().doubleValue());
					}
					else if (decimalRangeCriterium.includeLower()) {
						query = new BooleanQuery.Builder()
								.add(DoublePoint.newRangeQuery(
									fieldName,
									decimalRangeCriterium.getLowerValue().doubleValue(),
									decimalRangeCriterium.getUpperValue().doubleValue()), Occur.MUST)
								.add(DoublePoint.newExactQuery(fieldName, decimalRangeCriterium.getUpperValue().doubleValue()), Occur.MUST_NOT)
								.build();
					}
					else if (decimalRangeCriterium.includeUpper()) {
						query = new BooleanQuery.Builder()
								.add(DoublePoint.newRangeQuery(
									fieldName,
									decimalRangeCriterium.getLowerValue().doubleValue(),
									decimalRangeCriterium.getUpperValue().doubleValue()), Occur.MUST)
								.add(DoublePoint.newExactQuery(fieldName, decimalRangeCriterium.getLowerValue().doubleValue()), Occur.MUST_NOT)
								.build();
						
					}
					else {
						query = new BooleanQuery.Builder()
								.add(DoublePoint.newRangeQuery(
									fieldName,
									decimalRangeCriterium.getLowerValue().doubleValue(),
									decimalRangeCriterium.getUpperValue().doubleValue()), Occur.MUST)
								.add(DoublePoint.newExactQuery(fieldName, decimalRangeCriterium.getLowerValue().doubleValue()), Occur.MUST_NOT)
								.add(DoublePoint.newExactQuery(fieldName, decimalRangeCriterium.getUpperValue().doubleValue()), Occur.MUST_NOT)
								.build();
						
					}
					
					occur = Occur.MUST;
				}
				else {
					throw new UnsupportedOperationException("Reange query");
				}
			}
			else {
				throw new UnsupportedOperationException("Unknown criterium");
			}

			queryBuilder.add(query, occur);
		}
		
		final ResultsCollector resultsCollector;
		try {
			resultsCollector = new ResultsCollector();

			searcher.search(queryBuilder.build(), resultsCollector);
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

		return new IndexSearchCursor() {

			@Override
			public int getTotalMatchCount() {
				return documents.size();
			}

			@Override
			public List<String> getItemIDs(int initialIdx, int count) {
				
				return documents.stream()
						.map(d -> d.getField("id").stringValue())
						.skip(initialIdx)
						.limit(count)
						.collect(Collectors.toList());
			}

			@Override
			public ItemsFacets getFacets() {
				
				// Must find all distinct results of each attribute for the items
				return null;
			}
		};
	}
	
	
	private static class ResultsCollector implements Collector {
		private final List<Integer> documents;
		
		ResultsCollector() {
			this.documents = new ArrayList<>();
		}
		
		@Override
		public LeafCollector getLeafCollector(LeafReaderContext arg0) throws IOException {
			return new LeafCollector() {
				
				@Override
				public void setScorer(Scorer arg0) throws IOException {
					int a;
					
					a = 1;
				}
				
				@Override
				public void collect(int docId) throws IOException {
					documents.add(docId);
				}
			};
		}

		@Override
		public boolean needsScores() {
			return false;
		}
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
