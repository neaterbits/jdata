package com.test.salesportal.rest.search.all.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;

import com.test.salesportal.model.Item;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.sports.Snowboard;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.model.criteria.SearchCriteriumValue;
import com.test.salesportal.rest.search.model.criteria.SearchRange;

import junit.framework.TestCase;

public class SearchKeyMatchUtilTest extends TestCase {

	public void testFreetext() {

		final Snowboard snowboard = new Snowboard();
		
		snowboard.setTitle("Fole fint snowboard");
	
		assertThat(SearchKeyMatchUtil.matchesSearchKey(makeFreetextSearchKey(Snowboard.class, "Fole"), snowboard)).isTrue();
		assertThat(SearchKeyMatchUtil.matchesSearchKey(makeFreetextSearchKey(Snowboard.class, "Jaujau"), snowboard)).isFalse();
	}
	
	public void testSingleValueCriteria() {
		
		final Snowboard snowboard = new Snowboard();
		
		final TypeInfo snowboardType = ItemTypes.getTypeInfo(Snowboard.class);
		
		snowboard.setMake("Burton");
		
		SearchKey searchKey = makeCriteriaSearchKey(
				Snowboard.class,
				new SearchCriterium(
						snowboardType.getTypeName(),
						"make",
						new SearchCriteriumValue[] { new SearchCriteriumValue("Burton") },
						false));
		
		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isTrue();

		searchKey = makeCriteriaSearchKey(
				Snowboard.class,
				new SearchCriterium(
						snowboardType.getTypeName(),
						"make",
						new SearchCriteriumValue[] { new SearchCriteriumValue("Jones") },
						false));
		
		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isFalse();
	}

	public void testOtherSelectedCriteria() {
		
		final Snowboard snowboard = new Snowboard();
		
		final TypeInfo snowboardType = ItemTypes.getTypeInfo(Snowboard.class);
		
		snowboard.setMake("Burton");
		
		SearchKey searchKey = makeCriteriaSearchKey(
				Snowboard.class,
				new SearchCriterium(
						snowboardType.getTypeName(),
						"make",
						new SearchCriteriumValue[] { new SearchCriteriumValue("Jones") },
						false));
		
		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isFalse();

		searchKey = makeCriteriaSearchKey(
				Snowboard.class,
				new SearchCriterium(
						snowboardType.getTypeName(),
						"make",
						new SearchCriteriumValue[] { new SearchCriteriumValue("Jones") },
						true)); // otherSelected is true here
		
		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isTrue();
	}

	public void testSingleValueSubCriteria() {
		
		final Snowboard snowboard = new Snowboard();
		
		final TypeInfo snowboardType = ItemTypes.getTypeInfo(Snowboard.class);
		
		snowboard.setMake("Burton");
		snowboard.setModel("SomeModel");
		
		SearchKey searchKey = makeCriteriaSearchKey(
				Snowboard.class,
				new SearchCriterium(
					snowboardType.getTypeName(),
					"make",
					new SearchCriteriumValue[] {
						new SearchCriteriumValue(
							"Burton",
							new SearchCriterium [] {
								new SearchCriterium(
									snowboardType.getTypeName(),
									"model",
									new SearchCriteriumValue [] { new SearchCriteriumValue("SomeModel") },
									false)
							})
					},
					false));
		
		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isTrue();

		// Same as above but with SomeOtherModel as model so that does not match
		searchKey = makeCriteriaSearchKey(
				Snowboard.class,
				new SearchCriterium(
					snowboardType.getTypeName(),
					"make",
					new SearchCriteriumValue[] {
						new SearchCriteriumValue(
							"Burton",
							new SearchCriterium [] {
								new SearchCriterium(
									snowboardType.getTypeName(),
									"model",
									new SearchCriteriumValue [] { new SearchCriteriumValue("SomeOtherModel") },
									false)
							})
					},
					false));
		
		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isFalse();
	}
	
	public void testRangeCriteria() {

		final Snowboard snowboard = new Snowboard();
		
		final TypeInfo snowboardType = ItemTypes.getTypeInfo(Snowboard.class);
		
		snowboard.setWidth(decimal(165));

		SearchKey searchKey = makeCriteriaSearchKey(
				Snowboard.class,
				new SearchCriterium(
						snowboardType.getTypeName(),
						"width",
						new SearchRange [] {
							new SearchRange(null, true, decimal(160), false),
							new SearchRange(decimal(160), true, decimal(170), false),
							new SearchRange(decimal(170), true, null, false),
						}));
				
		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isTrue();

		searchKey = makeCriteriaSearchKey(
				Snowboard.class,
				new SearchCriterium(
						snowboardType.getTypeName(),
						"width",
						new SearchRange [] {
							new SearchRange(decimal(170), true, null, false),
						}));

		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isFalse();
	}
	
	public void testSingleValueAndRangeCriteria() {
		
		// Must match both in order to match search key
		final Snowboard snowboard = new Snowboard();
		
		final TypeInfo snowboardType = ItemTypes.getTypeInfo(Snowboard.class);
		
		snowboard.setMake("Burton");
		snowboard.setWidth(BigDecimal.valueOf(165));

		SearchKey searchKey = new SearchKey(
				Arrays.asList(Snowboard.class),
				null,
				new SearchCriterium [] {
					new SearchCriterium(
						snowboardType.getTypeName(),
						"make",
						new SearchCriteriumValue[] { new SearchCriteriumValue("Burton") },
						false),
					
					new SearchCriterium(
						snowboardType.getTypeName(),
						"width",
						new SearchRange [] {
							new SearchRange(null, true, decimal(160), false),
							new SearchRange(decimal(160), true, decimal(170), false),
							new SearchRange(decimal(170), true, null, false),
						})
				},
				null,
				null);

		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isTrue();

		searchKey = new SearchKey(
				Arrays.asList(Snowboard.class),
				null,
				new SearchCriterium [] {
					new SearchCriterium(
						snowboardType.getTypeName(),
						"make",
						new SearchCriteriumValue[] { new SearchCriteriumValue("Jones") },
						false),
					
					new SearchCriterium(
						snowboardType.getTypeName(),
						"width",
						new SearchRange [] {
							new SearchRange(null, true, decimal(160), false),
							new SearchRange(decimal(160), true, decimal(170), false),
							new SearchRange(decimal(170), true, null, false),
						})
				},
				null,
				null);
		
		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isFalse();

		searchKey = new SearchKey(
				Arrays.asList(Snowboard.class),
				null,
				new SearchCriterium [] {
					new SearchCriterium(
						snowboardType.getTypeName(),
						"make",
						new SearchCriteriumValue[] { new SearchCriteriumValue("Burton") },
						false),
					
					new SearchCriterium(
						snowboardType.getTypeName(),
						"width",
						new SearchRange [] {
							new SearchRange(decimal(170), true, null, false),
						})
				},
				null,
				null);

		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isFalse();
	}

	public void testFreetextAndCriteria() {
		
		// Must match both in order to match search key
		final Snowboard snowboard = new Snowboard();
		
		final TypeInfo snowboardType = ItemTypes.getTypeInfo(Snowboard.class);
		
		snowboard.setTitle("Burton snowboard for sale");
		snowboard.setMake("Burton");
		snowboard.setWidth(BigDecimal.valueOf(165));

		SearchKey searchKey = new SearchKey(
				Arrays.asList(Snowboard.class),
				"Burton",
				new SearchCriterium [] {
					new SearchCriterium(
						snowboardType.getTypeName(),
						"make",
						new SearchCriteriumValue[] { new SearchCriteriumValue("Burton") },
						false)
				},
				null,
				null);

		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isTrue();

		searchKey = new SearchKey(
				Arrays.asList(Snowboard.class),
				"Jones",
				new SearchCriterium [] {
					new SearchCriterium(
						snowboardType.getTypeName(),
						"make",
						new SearchCriteriumValue[] { new SearchCriteriumValue("Burton") },
						false)
				},
				null,
				null);

		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isFalse();
		
		searchKey = new SearchKey(
				Arrays.asList(Snowboard.class),
				"Burton",
				new SearchCriterium [] {
					new SearchCriterium(
						snowboardType.getTypeName(),
						"make",
						new SearchCriteriumValue[] { new SearchCriteriumValue("Jones") },
						false)
				},
				null,
				null);

		assertThat(SearchKeyMatchUtil.matchesSearchKey(searchKey, snowboard)).isFalse();
	}

	private static SearchKey makeFreetextSearchKey(Class<? extends Item> type, String freetext) {
		return new SearchKey(Arrays.asList(type), freetext, null, null, null);
	}

	private static SearchKey makeCriteriaSearchKey(Class<? extends Item> type, SearchCriterium ... criteria) {
		return new SearchKey(Arrays.asList(type), null, criteria, null, null);
	}
	
	private static BigDecimal decimal(int num) {
		return BigDecimal.valueOf(num);
	}
}
