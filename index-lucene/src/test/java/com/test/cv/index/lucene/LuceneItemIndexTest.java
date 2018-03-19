package com.test.cv.index.lucene;

import java.math.BigDecimal;
import java.util.Arrays;

import org.apache.lucene.store.RAMDirectory;

import com.test.cv.index.IndexSearchCursor;
import com.test.cv.index.ItemIndex;
import com.test.cv.index.ItemIndexException;
import com.test.cv.model.DecimalAttributeValue;
import com.test.cv.model.IntegerAttributeValue;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.StringAttributeValue;
import com.test.cv.model.attributes.ClassAttributes;
import com.test.cv.model.items.ItemTypes;
import com.test.cv.model.items.sports.Snowboard;
import com.test.cv.search.criteria.ComparisonOperator;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.criteria.DecimalCriterium;
import com.test.cv.search.criteria.DecimalRange;
import com.test.cv.search.criteria.DecimalRangesCriterium;
import com.test.cv.search.criteria.IntegerCriterium;
import com.test.cv.search.criteria.IntegerRange;
import com.test.cv.search.criteria.IntegerRangesCriterium;
import com.test.cv.search.criteria.StringCriterium;

import junit.framework.TestCase;

import static org.assertj.core.api.Assertions.*;

public class LuceneItemIndexTest extends TestCase {

	public void testAddAndSearch() throws Exception {

		try (LuceneItemIndex index = new LuceneItemIndex(new RAMDirectory())) {
		
			final ClassAttributes attributes = ClassAttributes.getFromClass(Snowboard.class);
			
			final ItemAttribute idAttribute = attributes.getByName("id");
			final ItemAttribute makeAttribute = attributes.getByName("make");
			final ItemAttribute yearAttribute = attributes.getByName("productionYear");
			final ItemAttribute widthAttribute = attributes.getByName("width");
			
			final StringAttributeValue idAttributeValue = new StringAttributeValue(idAttribute, "1234");
			final StringAttributeValue makeAttributeValue = new StringAttributeValue(makeAttribute, "Burton");
			final IntegerAttributeValue yearAttrbuteValue = new IntegerAttributeValue(yearAttribute, 2015);
			final DecimalAttributeValue widthAttributeValue = new DecimalAttributeValue(widthAttribute, new BigDecimal("32.5"));
	
			index.indexItemAttributes(Snowboard.class, ItemTypes.getTypeName(Snowboard.class), Arrays.asList(idAttributeValue, makeAttributeValue, yearAttrbuteValue, widthAttributeValue));

			IndexSearchCursor cursor = search(index, new StringCriterium(makeAttribute, "Burton", ComparisonOperator.EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			// Decimal value tests
			cursor = search(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.NOT_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = search(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = search(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = search(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.4"), ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.6"), ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = search(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.51"), ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.49"), ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			// Integer value tests
			cursor = search(index, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, new IntegerCriterium(yearAttribute, 2016, ComparisonOperator.NOT_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = search(index, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = search(index, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = search(index, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, new IntegerCriterium(yearAttribute, 2014, ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, new IntegerCriterium(yearAttribute, 2016, ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = search(index, new IntegerCriterium(yearAttribute, 2016, ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, new IntegerCriterium(yearAttribute, 2014, ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			// Integer range tests
			cursor = search(index, makeIntegerRangeCriterium(yearAttribute, 2015, false, 2017, false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = search(index, makeIntegerRangeCriterium(yearAttribute, 2015, true, 2017, false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, makeIntegerRangeCriterium(yearAttribute, 2014, false, 2016, false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, makeIntegerRangeCriterium(yearAttribute, 2013, false, 2015, false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = search(index, makeIntegerRangeCriterium(yearAttribute, 2013, false, 2015, true));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			// Decimal range tests
			cursor = search(index, makeDecimalRangeCriterium(widthAttribute, new BigDecimal("32.5"), false, new BigDecimal("40.2"), false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = search(index, makeDecimalRangeCriterium(widthAttribute, new BigDecimal("32.5"), true, new BigDecimal("40.2"), false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, makeDecimalRangeCriterium(widthAttribute, new BigDecimal("30.5"), false, new BigDecimal("40.2"), false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = search(index, makeDecimalRangeCriterium(widthAttribute, new BigDecimal("30.5"), false, new BigDecimal("32.5"), false));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = search(index, makeDecimalRangeCriterium(widthAttribute, new BigDecimal("30.5"), false, new BigDecimal("32.5"), true));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);
		}
		catch (Throwable t) {
			System.err.println("Caught throwable " + t);
			t.printStackTrace();
			throw t;
		}
	}

	private static IntegerRangesCriterium makeIntegerRangeCriterium(ItemAttribute attribute, Integer lowerValue, boolean includeLower, Integer upperValue, boolean includeUpper) {
		final IntegerRange integerRange = new IntegerRange(lowerValue, includeLower, upperValue, includeUpper);
		
		return new IntegerRangesCriterium(attribute, new IntegerRange [] { integerRange });
	}
	
	private static DecimalRangesCriterium makeDecimalRangeCriterium(ItemAttribute attribute, BigDecimal lowerValue, boolean includeLower, BigDecimal upperValue, boolean includeUpper) {
		final DecimalRange decimalRange = new DecimalRange(lowerValue, includeLower, upperValue, includeUpper);
		
		return new DecimalRangesCriterium(attribute, new DecimalRange [] { decimalRange });
	}
	
	private static IndexSearchCursor search(ItemIndex index, Criterium criterium) throws ItemIndexException {
		return index.search(null, Arrays.asList(criterium), null);
	}
}

