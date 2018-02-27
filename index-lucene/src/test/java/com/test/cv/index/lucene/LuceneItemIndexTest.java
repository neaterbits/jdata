package com.test.cv.index.lucene;

import java.math.BigDecimal;
import java.util.Arrays;

import org.apache.lucene.store.RAMDirectory;

import com.test.cv.index.IndexSearchCursor;
import com.test.cv.model.DecimalAttributeValue;
import com.test.cv.model.IntegerAttributeValue;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.StringAttributeValue;
import com.test.cv.model.attributes.ClassAttributes;
import com.test.cv.model.items.Snowboard;
import com.test.cv.search.criteria.ComparisonOperator;
import com.test.cv.search.criteria.DecimalCriterium;
import com.test.cv.search.criteria.DecimalRangeCriterium;
import com.test.cv.search.criteria.IntegerCriterium;
import com.test.cv.search.criteria.IntegerRangeCriterium;
import com.test.cv.search.criteria.StringCriterium;

import junit.framework.TestCase;

import static org.assertj.core.api.Assertions.*;

public class LuceneItemIndexTest extends TestCase {

	public void testAddAndSearch() throws Exception {

		try (LuceneItemIndex index = new LuceneItemIndex(new RAMDirectory())) {
		
			final ClassAttributes attributes = ClassAttributes.getFromClass(Snowboard.class);
			
			final ItemAttribute makeAttribute = attributes.getByName("make");
			final ItemAttribute yearAttribute = attributes.getByName("productionYear");
			final ItemAttribute widthAttribute = attributes.getByName("width");
			
			final StringAttributeValue makeAttributeValue = new StringAttributeValue(makeAttribute, "Burton");
			final IntegerAttributeValue yearAttrbuteValue = new IntegerAttributeValue(yearAttribute, 2015);
			final DecimalAttributeValue widthAttributeValue = new DecimalAttributeValue(widthAttribute, new BigDecimal("32.5"));
	
			index.indexItemAttributes(Snowboard.class, Arrays.asList(makeAttributeValue, yearAttrbuteValue, widthAttributeValue));

			IndexSearchCursor cursor = index.search(null, new StringCriterium(makeAttribute, "Burton", ComparisonOperator.EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			// Decimal value tests
			cursor = index.search(null, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.NOT_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = index.search(null, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = index.search(null, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = index.search(null, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new DecimalCriterium(widthAttribute, new BigDecimal("32.4"), ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new DecimalCriterium(widthAttribute, new BigDecimal("32.6"), ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = index.search(null, new DecimalCriterium(widthAttribute, new BigDecimal("32.51"), ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new DecimalCriterium(widthAttribute, new BigDecimal("32.49"), ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			// Integer value tests
			cursor = index.search(null, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new IntegerCriterium(yearAttribute, 2016, ComparisonOperator.NOT_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = index.search(null, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = index.search(null, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = index.search(null, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new IntegerCriterium(yearAttribute, 2014, ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new IntegerCriterium(yearAttribute, 2016, ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = index.search(null, new IntegerCriterium(yearAttribute, 2016, ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new IntegerCriterium(yearAttribute, 2014, ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			// Integer range tests
			cursor = index.search(null, new IntegerRangeCriterium(yearAttribute, 2015, false, 2017, false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = index.search(null, new IntegerRangeCriterium(yearAttribute, 2015, true, 2017, false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new IntegerRangeCriterium(yearAttribute, 2014, false, 2016, false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new IntegerRangeCriterium(yearAttribute, 2013, false, 2015, false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = index.search(null, new IntegerRangeCriterium(yearAttribute, 2013, false, 2015, true));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			// Decimal range tests
			cursor = index.search(null, new DecimalRangeCriterium(widthAttribute, new BigDecimal("32.5"), false, new BigDecimal("40.2"), false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = index.search(null, new DecimalRangeCriterium(widthAttribute, new BigDecimal("32.5"), true, new BigDecimal("40.2"), false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new DecimalRangeCriterium(widthAttribute, new BigDecimal("30.5"), false, new BigDecimal("40.2"), false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = index.search(null, new DecimalRangeCriterium(widthAttribute, new BigDecimal("30.5"), false, new BigDecimal("32.5"), false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = index.search(null, new DecimalRangeCriterium(widthAttribute, new BigDecimal("30.5"), false, new BigDecimal("32.5"), true));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);
		}
		catch (Throwable t) {
			System.err.println("Caught throwable " + t);
			t.printStackTrace();
			throw t;
		}
	}
}

