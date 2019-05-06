package com.test.salesportal.index.lucene;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import com.test.salesportal.common.IOUtil;
import com.test.salesportal.index.IndexSearchCursor;
import com.test.salesportal.index.ItemIndex;
import com.test.salesportal.index.ItemIndexException;
import com.test.salesportal.index.lucene.LuceneItemIndex;
import com.test.salesportal.model.DecimalAttributeValue;
import com.test.salesportal.model.IntegerAttributeValue;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.StringAttributeValue;
import com.test.salesportal.model.attributes.ClassAttributes;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.sports.Snowboard;
import com.test.salesportal.model.items.vehicular.Car;
import com.test.salesportal.model.items.vehicular.Fuel;
import com.test.salesportal.search.SearchItem;
import com.test.salesportal.search.criteria.ComparisonOperator;
import com.test.salesportal.search.criteria.Criterium;
import com.test.salesportal.search.criteria.DecimalCriterium;
import com.test.salesportal.search.criteria.DecimalRange;
import com.test.salesportal.search.criteria.DecimalRangesCriterium;
import com.test.salesportal.search.criteria.EnumInCriterium;
import com.test.salesportal.search.criteria.InCriteriumValue;
import com.test.salesportal.search.criteria.IntegerCriterium;
import com.test.salesportal.search.criteria.IntegerRange;
import com.test.salesportal.search.criteria.IntegerRangesCriterium;
import com.test.salesportal.search.criteria.StringCriterium;

import junit.framework.TestCase;

import static org.assertj.core.api.Assertions.*;

public class LuceneItemIndexTest extends TestCase {

	public void testAddAndSearch() throws Exception {

		final String userId = "user123";
		
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
	
			index.indexItemAttributes(userId, Snowboard.class, ItemTypes.getTypeName(Snowboard.class), Arrays.asList(idAttributeValue, makeAttributeValue, yearAttrbuteValue, widthAttributeValue));

			IndexSearchCursor cursor = searchSnowboads(index, new StringCriterium(makeAttribute, "Burton", ComparisonOperator.EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			// Decimal value tests
			cursor = searchSnowboads(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.NOT_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = searchSnowboads(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = searchSnowboads(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = searchSnowboads(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.5"), ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.4"), ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.6"), ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = searchSnowboads(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.51"), ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, new DecimalCriterium(widthAttribute, new BigDecimal("32.49"), ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			// Integer value tests
			cursor = searchSnowboads(index, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, new IntegerCriterium(yearAttribute, 2016, ComparisonOperator.NOT_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = searchSnowboads(index, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = searchSnowboads(index, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = searchSnowboads(index, new IntegerCriterium(yearAttribute, 2015, ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, new IntegerCriterium(yearAttribute, 2014, ComparisonOperator.GREATER_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, new IntegerCriterium(yearAttribute, 2016, ComparisonOperator.GREATER_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			cursor = searchSnowboads(index, new IntegerCriterium(yearAttribute, 2016, ComparisonOperator.LESS_THAN));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, new IntegerCriterium(yearAttribute, 2014, ComparisonOperator.LESS_THAN_OR_EQUALS));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);

			// TODO test include no-value as well
			final boolean includeWithNoValue = false;
			
			// Integer range tests
			cursor = searchSnowboads(index, makeIntegerRangeCriterium(yearAttribute, 2015, false, 2017, false, includeWithNoValue));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = searchSnowboads(index, makeIntegerRangeCriterium(yearAttribute, 2015, true, 2017, false, includeWithNoValue));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, makeIntegerRangeCriterium(yearAttribute, 2014, false, 2016, false, includeWithNoValue));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, makeIntegerRangeCriterium(yearAttribute, 2013, false, 2015, false, includeWithNoValue));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = searchSnowboads(index, makeIntegerRangeCriterium(yearAttribute, 2013, false, 2015, true, includeWithNoValue));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			// Decimal range tests
			cursor = searchSnowboads(index, makeDecimalRangeCriterium(widthAttribute, new BigDecimal("32.5"), false, new BigDecimal("40.2"), false, includeWithNoValue));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = searchSnowboads(index, makeDecimalRangeCriterium(widthAttribute, new BigDecimal("32.5"), true, new BigDecimal("40.2"), false, includeWithNoValue));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, makeDecimalRangeCriterium(widthAttribute, new BigDecimal("30.5"), false, new BigDecimal("40.2"), false, includeWithNoValue));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);

			cursor = searchSnowboads(index, makeDecimalRangeCriterium(widthAttribute, new BigDecimal("30.5"), false, new BigDecimal("32.5"), false, includeWithNoValue));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(0);
	
			cursor = searchSnowboads(index, makeDecimalRangeCriterium(widthAttribute, new BigDecimal("30.5"), false, new BigDecimal("32.5"), true, includeWithNoValue));
			assertThat(cursor.getTotalMatchCount()).isEqualTo(1);
		}
		catch (Throwable t) {
			System.err.println("Caught throwable " + t);
			t.printStackTrace();
			throw t;
		}
	}
	
	public void testThumbnail() throws IOException, Exception {

		final String userId = "user123";

		try (LuceneItemIndex index = new LuceneItemIndex(new RAMDirectory())) {
			
			final ClassAttributes attributes = ClassAttributes.getFromClass(Snowboard.class);
			
			final ItemAttribute idAttribute = attributes.getByName("id");
			final ItemAttribute makeAttribute = attributes.getByName("make");
			
			final String itemId = "1234-5678";
			
			final StringAttributeValue idAttributeValue = new StringAttributeValue(idAttribute, itemId);
			final StringAttributeValue makeAttributeValue = new StringAttributeValue(makeAttribute, "Burton");
	
			index.indexItemAttributes(userId, Snowboard.class, ItemTypes.getTypeName(Snowboard.class), Arrays.asList(idAttributeValue, makeAttributeValue));
			
			index.indexThumbnailSize(itemId, Snowboard.class, 0, 320, 240);
			index.indexThumbnailSize(itemId, Snowboard.class, 1, 300, 250);
			index.indexThumbnailSize(itemId, Snowboard.class, 2, 290, 340);

			final IndexSearchCursor searchCursor = index.search(
					Arrays.asList(Snowboard.class),
					null,
					Arrays.asList(new StringCriterium(idAttribute, itemId, ComparisonOperator.EQUALS)),
					null,
					false,
					null,
					null);
			
			final SearchItem item = searchCursor.getItemIDsAndTitles(0, 1).get(0);
			
			assertThat(item.getThumbWidth()).isEqualTo(320);
			assertThat(item.getThumbHeight()).isEqualTo(240);
		}
	}

	
	public void testAddAndSearchToFileIndex() throws Exception {
		// Issue when adding multiple entries to a index on file
		final File directory = IOUtil.makeTempFileAndDeleteOnExit("luceneindex");

		final Car car1 = new Car();
		car1.setIdString("itemId1");
		car1.setMake("Honda");
		car1.setFuel(Fuel.GAS);
		
		final Car car2 = new Car();
		
		car2.setIdString("itemId2");
		car2.setMake("Toyota");
		car2.setFuel(Fuel.GAS);
		
		final String userId = "theUser";

		openIndexAndStoreAndCloseIndex(userId, car1, FSDirectory.open(directory.toPath()));
		openIndexAndStoreAndCloseIndex(userId, car2, FSDirectory.open(directory.toPath()));
		
		try (LuceneItemIndex index = new LuceneItemIndex(directory.getAbsolutePath())) {
			
			// index.indexItemAttributes(userId, Car.class, ItemTypes.getTypeName(Car.class), ClassAttributes.getValues(car1));
			// index.indexItemAttributes(userId, Car.class, ItemTypes.getTypeName(Car.class), ClassAttributes.getValues(car2));
			
			final TypeInfo carTypeInfo = ItemTypes.getTypeInfo(Car.class);
			
			// Search by make should return 2 entries
			final Criterium criterium = new EnumInCriterium<>(
					carTypeInfo.getAttributes().getByName("fuel"),
					Arrays.asList(new InCriteriumValue<Fuel>(Fuel.GAS, null)),
					false);
			final IndexSearchCursor cursor = index.search(
					Arrays.asList(Snowboard.class),
					null,
					Arrays.asList(criterium),
					null,
					false,
					null,
					null);

			assertThat(cursor.getTotalMatchCount()).isEqualTo(2);
		}
	}
	
	private void openIndexAndStoreAndCloseIndex(String userId, Car car, Directory directory) throws IOException, Exception {
		try (LuceneItemIndex index = new LuceneItemIndex(directory)) {
			final Car car1 = new Car();
			car1.setIdString("itemId1");
			car1.setMake("Honda");
			car1.setFuel(Fuel.GAS);

			index.indexItemAttributes(userId, Car.class, ItemTypes.getTypeName(Car.class), ClassAttributes.getValues(car1));
		}
	}
	
	private static IntegerRangesCriterium makeIntegerRangeCriterium(ItemAttribute attribute, Integer lowerValue, boolean includeLower, Integer upperValue, boolean includeUpper, boolean includeItemsWithNoValue) {
		final IntegerRange integerRange = new IntegerRange(lowerValue, includeLower, upperValue, includeUpper);
		
		return new IntegerRangesCriterium(attribute, new IntegerRange [] { integerRange }, includeItemsWithNoValue);
	}
	
	private static DecimalRangesCriterium makeDecimalRangeCriterium(ItemAttribute attribute, BigDecimal lowerValue, boolean includeLower, BigDecimal upperValue, boolean includeUpper, boolean includeItemsWithNoValue) {
		final DecimalRange decimalRange = new DecimalRange(lowerValue, includeLower, upperValue, includeUpper);
		
		return new DecimalRangesCriterium(attribute, new DecimalRange [] { decimalRange }, includeItemsWithNoValue);
	}

	private static IndexSearchCursor searchSnowboads(ItemIndex index, Criterium criterium) throws ItemIndexException {
		return index.search(Arrays.asList(Snowboard.class), null, Arrays.asList(criterium), null, false, null, null);
	}
}

