package com.test.cv.dao.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.persistence.MapKeyEnumerated;

import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.SearchException;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.attributes.ClassAttributes;
import com.test.cv.model.items.sports.Snowboard;
import com.test.cv.model.items.sports.SnowboardProfile;
import com.test.cv.model.items.vehicular.Car;
import com.test.cv.search.SearchItem;
import com.test.cv.search.criteria.ComparisonOperator;
import com.test.cv.search.criteria.Criterium;
import com.test.cv.search.criteria.DecimalCriterium;
import com.test.cv.search.criteria.DecimalInCriterium;
import com.test.cv.search.criteria.InCriteriumValue;
import com.test.cv.search.criteria.NoValueCriterium;
import com.test.cv.search.criteria.StringInCriterium;
import com.test.cv.search.facets.IndexFacetedAttributeResult;
import com.test.cv.search.facets.IndexRangeFacetedAttributeResult;
import com.test.cv.search.facets.IndexSingleValueFacet;
import com.test.cv.search.facets.IndexSingleValueFacetedAttributeResult;
import com.test.cv.search.facets.ItemsFacets;
import com.test.cv.search.facets.TypeFacets;

import junit.framework.TestCase;

public abstract class SearchDAOTest extends TestCase {

	protected abstract IItemDAO getItemDAO();
	protected abstract ISearchDAO getSearchDAO();

	static Snowboard makeSnowboard1() {
		final Snowboard snowboard = new Snowboard();
		
		snowboard.setTitle("First snowboard for sale");
		snowboard.setMake("Burton");
		snowboard.setModel("1234");
		snowboard.setProfile(SnowboardProfile.CAMBER);
		snowboard.setHeight(new BigDecimal("2.5"));
		snowboard.setWidth(new BigDecimal("30.4"));
		snowboard.setLength(new BigDecimal("164.5"));

		return snowboard;
	}
	
	static Snowboard makeSnowboard2() {
		final Snowboard snowboard = new Snowboard();
		
		snowboard.setTitle("Second snowboard for sale");
		snowboard.setMake("Jones");
		snowboard.setModel("Abcd");
		snowboard.setProfile(SnowboardProfile.FLAT);
		snowboard.setHeight(new BigDecimal("1.9"));
		snowboard.setWidth(new BigDecimal("32.8"));
		snowboard.setLength(new BigDecimal("167.3"));

		return snowboard;
	}
	
	public void testSearchWithNullValueForTypesThrowsException() throws Exception {
		try (ISearchDAO searchDAO = getSearchDAO()) {

			try {
				searchDAO.search(null, null, null, null);
				
				fail("Expected IllegalArgumentException because of types == null");
			}
			catch (IllegalArgumentException ex){
				
			}
		}
	}

	public void testSearchWithNullValueForCriteriumIsOk() throws Exception {
		checkSnowboard((userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ISearchCursor cursor = searchDAO.search(Arrays.asList(Snowboard.class), null, null, null);

			final List<String> itemIds = cursor.getAllItemIDs();
			assertThat(itemIds.size()).isEqualTo(2);
			assertThat(itemIds.contains(itemId1)).isTrue();
			assertThat(itemIds.contains(itemId2)).isTrue();
		});
	}

	// Test with empty types list ought to give empty result set
	public void testSearchWithEmptyTypeListGivesEmptyResult() throws Exception {
		checkSnowboard((userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ISearchCursor cursor = searchDAO.search(new ArrayList<>(), null, null, null);

			final List<String> itemIds = cursor.getAllItemIDs();
			assertThat(itemIds.size()).isEqualTo(0);
		});
	}

	// Store more that one type
	public void testSearchWithOneOfMultipleTypes() throws Exception {

		final String userId = "theUser";
		
		try (IItemDAO itemDAO = getItemDAO()) {
		
			final Car car = new Car();
			
			car.setMake("Totyota");
			car.setModel("Corolla");
			

			itemDAO.addItem(userId, car);

			final String carItemId = car.getIdString();
			assertThat(carItemId).isNotNull();

			final Snowboard snowboard = makeSnowboard1();

			itemDAO.addItem(userId, snowboard);

			try (ISearchDAO searchDAO = getSearchDAO()) {

				final ISearchCursor cursor = searchDAO.search(Arrays.asList(Car.class), null, null, null);
				final List<String> itemIds = cursor.getAllItemIDs();

				assertThat(itemIds.size()).isEqualTo(1);
				assertThat(itemIds.get(0)).isEqualTo(carItemId);
			}
		}
	}
	
	public void testSearchNoCriteria() throws Exception {

		checkSnowboard((userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					null,
					null,
					null);

			 assertThat(search.getTotalMatchCount()).isEqualTo(2);
			 
			 List<String> itemIds = search.getItemIDs(0, Integer.MAX_VALUE);

			 assertThat(itemIds.size()).isEqualTo(2);
			 assertThat(itemIds.contains(itemId1)).isTrue();
			 assertThat(itemIds.contains(itemId2)).isTrue();
		});
	}

	public void testSearchItemsNoCriteria() throws Exception {

		checkSnowboard((userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					null,
					null,
					null);

			 assertThat(search.getTotalMatchCount()).isEqualTo(2);
			 
			 final List<SearchItem> items = search.getItemIDsAndTitles(0, Integer.MAX_VALUE);

			 assertThat(items.size()).isEqualTo(2);
			 
			 final SearchItem item1 = find(items, item -> item.getItemId().equals(itemId1));
			 final SearchItem item2 = find(items, item -> item.getItemId().equals(itemId2));

			 assertThat(item1).isNotNull();
			 assertThat(item2).isNotNull();
			 
			 assertThat(item1.getTitle()).isEqualTo("First snowboard for sale");
			 assertThat(item2.getTitle()).isEqualTo("Second snowboard for sale");
		});
	}

	private static <T> boolean contains(Collection<T> collection, Predicate<T> predicate) {
		return collection.stream().anyMatch(predicate);
	}
	
	private static <T> T find(Collection<T> collection, Predicate<T> predicate) {
		return collection.stream().filter(predicate).findFirst().get();
	}

	public void testSnowboardCriteriaGreaterThan() throws Exception {
		checkSnowboard((userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

			final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");
			 
			assertThat(widthAttribute).isNotNull();
			 
			final DecimalCriterium widthCriteria = new DecimalCriterium(widthAttribute, new BigDecimal("30.4"), ComparisonOperator.GREATER_THAN);
			
			final ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					Arrays.asList(widthCriteria),
					null,
					null);
			final List<String> itemIds = search.getItemIDs(0, Integer.MAX_VALUE);
			
			assertThat(itemIds.size()).isEqualTo(1);
			assertThat(itemIds.contains(itemId2)).isTrue();
		});
	}

	public void testSnowboardCriteriaNotEquals() throws Exception {
		checkSnowboard((userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

			final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");
			 
			assertThat(widthAttribute).isNotNull();
			 
			final DecimalCriterium widthCriteria = new DecimalCriterium(widthAttribute, new BigDecimal("32.8"), ComparisonOperator.NOT_EQUALS);
			
			final ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					Arrays.asList(widthCriteria),
					null,
					null);
			final List<String> itemIds = search.getItemIDs(0, Integer.MAX_VALUE);
			
			assertThat(itemIds.size()).isEqualTo(1);
			assertThat(itemIds.contains(itemId1)).isTrue();
		});
	}
	
	public void testSnowboardIn() throws Exception {
		checkSnowboard((userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

			final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");
			 
			assertThat(widthAttribute).isNotNull();
			 
			DecimalInCriterium widthCriterium = new DecimalInCriterium(widthAttribute, values(new BigDecimal("30.4"), new BigDecimal("32.8")), false);
			
			ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					Arrays.asList(widthCriterium),
					null,
					null);
			
			List<String> itemIds = search.getItemIDs(0, Integer.MAX_VALUE);
			
			assertThat(itemIds.size()).isEqualTo(2);
			assertThat(itemIds.contains(itemId1)).isTrue();
			assertThat(itemIds.contains(itemId2)).isTrue();
			
			// match only one
			widthCriterium = new DecimalInCriterium(widthAttribute, values(new BigDecimal("32.4"), new BigDecimal("32.8")), false);
			
			search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					Arrays.asList(widthCriterium),
					null,
					null);
			
			itemIds = search.getItemIDs(0, Integer.MAX_VALUE);
			
			assertThat(itemIds.size()).isEqualTo(1);
			assertThat(itemIds.contains(itemId2)).isTrue();
		});
		
	}
	
	
	private static List<InCriteriumValue<BigDecimal>> values(BigDecimal ... values) {
		final List<InCriteriumValue<BigDecimal>> list = new ArrayList<>(values.length);
		
		for (BigDecimal value : values) {
			list.add(new InCriteriumValue<BigDecimal>(value, null));
		}
		
		return list;
	}
	
	public void testFacetsNoCriteria() throws Exception {
		
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

		final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");
		final ItemAttribute makeAttribute = snowboardAttributes.getByName("make");

		final Set<ItemAttribute> facetedAttributes = new HashSet<>(Arrays.asList(widthAttribute, makeAttribute));
		
		checkSnowboard((userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			
			// Add one more snowboard with no make nor width
			final Snowboard snowboard3 = new Snowboard();
			String itemId3 = null;

			try {
				itemId3 = itemDAO.addItem(userId, snowboard3);

				final ISearchCursor search = searchDAO.search(
						Arrays.asList(Snowboard.class),
						null,
						null,
						facetedAttributes);
				
				final ItemsFacets facets = search.getFacets();
				
				assertThat(facets).isNotNull();
				assertThat(facets.getTypes().size()).isEqualTo(1);
	
				final TypeFacets typeFacets = facets.getTypes().get(0);
				
				assertThat(typeFacets.getType()).isEqualTo(Snowboard.class);
	
				System.out.println("Attributes: " + typeFacets.getAttributes());
				assertThat(typeFacets.getAttributes().size()).isEqualTo(2);
	
				final IndexSingleValueFacetedAttributeResult makeFacet =
						(IndexSingleValueFacetedAttributeResult) find(
								typeFacets.getAttributes(),
								attribute -> attribute.getAttribute().getName().equals("make"));
				
				assertThat(makeFacet).isNotNull();
				assertThat(makeFacet.getAttribute()).isEqualTo(makeAttribute);
				assertThat(makeFacet.getValues().size()).isEqualTo(2);
				
				assertThat(makeFacet.getValues().get(0).getMatchCount()).isEqualTo(1);
				assertThat(makeFacet.getValues().get(1).getMatchCount()).isEqualTo(1);
				
				// Should have one item that is missing this value
				assertThat(makeFacet.getNoAttributeValueCount()).isEqualTo(1);
	
				final IndexSingleValueFacet burton = find(makeFacet.getValues(), f -> f.getValue().equals("Burton"));
				assertThat(burton).isNotNull();
	
				final IndexSingleValueFacet jones = find(makeFacet.getValues(), f -> f.getValue().equals("Jones"));
				assertThat(jones).isNotNull();
	
				final IndexRangeFacetedAttributeResult widthFacet =
						(IndexRangeFacetedAttributeResult) find(
								typeFacets.getAttributes(),
								attribute -> attribute.getAttribute().getName().equals("width"));
	
				assertThat(widthFacet).isNotNull();
				assertThat(widthFacet.getAttribute()).isEqualTo(widthAttribute);
				assertThat(widthFacet.getMatchCounts()).isNotNull();
				assertThat(widthFacet.getMatchCounts().length).isEqualTo(widthAttribute.getDecimalRanges().length);
				assertThat(widthFacet.getMatchCounts()[0]).isEqualTo(0);
				assertThat(widthFacet.getMatchCounts()[1]).isEqualTo(0);
				assertThat(widthFacet.getMatchCounts()[2]).isEqualTo(1);
				assertThat(widthFacet.getMatchCounts()[3]).isEqualTo(1);
				assertThat(widthFacet.getMatchCounts()[4]).isEqualTo(0);

				// Should have one item that is missing this value
				assertThat(widthFacet.getNoAttributeValueCount()).isEqualTo(1);
			}
			finally {
				if (itemId3 != null) {
					itemDAO.deleteItem(userId, itemId3, Snowboard.class);
				}
			}
			
		});
	}

	public void testSearchReturnsOnlyOneMakeEvenIfSameModel() throws Exception {
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

		final ItemAttribute makeAttribute = snowboardAttributes.getByName("make");
		final ItemAttribute modelAttribute = snowboardAttributes.getByName("model");

		final Set<ItemAttribute> facetedAttributes = new HashSet<>(Arrays.asList(makeAttribute));

		final Snowboard snowboard1 = makeSnowboard1();
		final Snowboard snowboard2 = makeSnowboard2();

		// Different make but same model, should only return model from one of the makes
		assertThat(snowboard1.getMake()).isNotEqualTo(snowboard2.getMake());

		final String sameModel = "SnowboardModel";

		snowboard1.setModel(sameModel);
		snowboard2.setModel(sameModel);
		
		final List<InCriteriumValue<String>> modelValues = Arrays.asList(new InCriteriumValue<String>(sameModel, null));
		final StringInCriterium modelSubCriterium = new StringInCriterium(modelAttribute, modelValues, false);

		final StringInCriterium makeCriterium = new StringInCriterium(
				makeAttribute,
				Arrays.asList(new InCriteriumValue<String>(snowboard1.getMake(), Arrays.asList(modelSubCriterium))),
				false);
		
		final List<Criterium> criteria = Arrays.asList(makeCriterium);

		checkSnowboard(snowboard1, snowboard2, (userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ISearchCursor cursor = searchDAO.search(Arrays.asList(Snowboard.class), criteria, null, facetedAttributes);
			
			assertThat(cursor.getItemIDs(0, Integer.MAX_VALUE).size()).isEqualTo(1);
			assertThat(cursor.getItemIDs(0, Integer.MAX_VALUE).get(0)).isEqualTo(itemId1);
			
			assertThat(cursor.getFacets().getTypes().size()).isEqualTo(1);
			final TypeFacets typeFacets = cursor.getFacets().getTypes().get(0);
			
			assertThat(typeFacets.getType()).isEqualTo(Snowboard.class);
			assertThat(typeFacets.getAttributes().size()).isEqualTo(1);
			assertThat(typeFacets.getAttributes().get(0).getAttribute().getName()).isEqualTo("make");
			assertThat(typeFacets.getAttributes().get(0).getNoAttributeValueCount()).isEqualTo(0);
		});
	}
	
	@SafeVarargs
	private static <T extends Comparable<T>> List<InCriteriumValue<T>> values(T ... values)  {
		final List<InCriteriumValue<T>> list = new ArrayList<>(values.length);

		for (T value : values) {
			list.add(new InCriteriumValue<T>(value, null));
		}

		return list;
	}

	public void testSearchReturnsOnlyOneWhenSameMakeButDifferentModel() throws Exception {
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

		final ItemAttribute makeAttribute = snowboardAttributes.getByName("make");
		final ItemAttribute modelAttribute = snowboardAttributes.getByName("model");

		final Set<ItemAttribute> facetedAttributes = new HashSet<>(Arrays.asList(makeAttribute));

		final Snowboard snowboard1 = makeSnowboard1();
		final Snowboard snowboard2 = makeSnowboard2();

		// Different make but same model, should only return model from one of the makes
		assertThat(snowboard1.getMake()).isNotEqualTo(snowboard2.getMake());

		final String sameMake = "SameMake";
		snowboard1.setMake(sameMake);
		snowboard2.setMake(sameMake);

		final String model1 = "SnowboardModel1";
		final String model2 = "SnowboardModel2";
		
		snowboard1.setModel(model1);
		snowboard2.setModel(model2);
		
		final List<InCriteriumValue<String>> modelValues = Arrays.asList(new InCriteriumValue<String>(model2, null));
		final StringInCriterium modelSubCriterium = new StringInCriterium(modelAttribute, modelValues, false);

		final StringInCriterium makeCriterium = new StringInCriterium(
				makeAttribute,
				Arrays.asList(new InCriteriumValue<String>(sameMake, Arrays.asList(modelSubCriterium))),
				false);
		

		checkSnowboard(snowboard1, snowboard2, (userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ISearchCursor cursor = searchDAO.search(Arrays.asList(Snowboard.class), Arrays.asList(makeCriterium), null, facetedAttributes);

			final List<String> itemIds = cursor.getItemIDs(0, Integer.MAX_VALUE);

			assertThat(itemIds.size()).isEqualTo(1);
			assertThat(itemIds.get(0)).isEqualTo(itemId2);
			
			assertThat(cursor.getFacets().getTypes().size()).isEqualTo(1);
			final TypeFacets typeFacets = cursor.getFacets().getTypes().get(0);
			
			assertThat(typeFacets.getType()).isEqualTo(Snowboard.class);
			assertThat(typeFacets.getAttributes().size()).isEqualTo(1);
			assertThat(typeFacets.getAttributes().get(0).getAttribute().getName()).isEqualTo("make");
			assertThat(typeFacets.getAttributes().get(0).getNoAttributeValueCount()).isEqualTo(0);
		});
	}

	public void testReturnsAttributesEvenIfOnlyHasNoMatch() throws Exception {
		// Issue: if only matching sub-attribute on no-value, we should return a subattribute for that
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

		final ItemAttribute makeAttribute = snowboardAttributes.getByName("make");
		final ItemAttribute modelAttribute = snowboardAttributes.getByName("model");

		final Set<ItemAttribute> facetedAttributes = new HashSet<>(Arrays.asList(makeAttribute, modelAttribute));

		final Snowboard snowboard1 = makeSnowboard1();
		final Snowboard snowboard2 = makeSnowboard2();

		// Different make but same model, should only return model from one of the makes
		assertThat(snowboard1.getMake()).isNotEqualTo(snowboard2.getMake());

		snowboard1.setMake(null);
		
		snowboard1.setModel("Abc");
		snowboard2.setModel(null);

		final NoValueCriterium makeCriterium = new NoValueCriterium(makeAttribute);
		
		checkSnowboard(snowboard1, snowboard2, (userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ISearchCursor cursor = searchDAO.search(Arrays.asList(Snowboard.class), Arrays.asList(makeCriterium), null, facetedAttributes);

			final List<String> itemIds = cursor.getItemIDs(0, Integer.MAX_VALUE);

			assertThat(itemIds.size()).isEqualTo(1);
			assertThat(itemIds.get(0)).isEqualTo(itemId1);
			
			assertThat(cursor.getFacets().getTypes().size()).isEqualTo(1);
			final TypeFacets typeFacets = cursor.getFacets().getTypes().get(0);
			
			assertThat(typeFacets.getType()).isEqualTo(Snowboard.class);
			assertThat(typeFacets.getAttributes().size()).isEqualTo(1);
			assertThat(typeFacets.getAttributes().get(0).getAttribute().getName()).isEqualTo("make");
			assertThat(typeFacets.getAttributes().get(0).getNoAttributeValueCount()).isEqualTo(1);
			
			final IndexSingleValueFacetedAttributeResult makeFacet = (IndexSingleValueFacetedAttributeResult)typeFacets.getAttributes().get(0);
			
			assertThat(makeFacet.getValues().size()).isEqualTo(0);

		});
	}

	public void testReturnsSubAttributesEvenIfOnlyHasNoMatch() throws Exception {
		// Issue: if only matching sub-attribute on no-value, we should return a subattribute for that
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

		final ItemAttribute makeAttribute = snowboardAttributes.getByName("make");
		final ItemAttribute modelAttribute = snowboardAttributes.getByName("model");

		final Set<ItemAttribute> facetedAttributes = new HashSet<>(Arrays.asList(makeAttribute, modelAttribute));

		final Snowboard snowboard1 = makeSnowboard1();
		final Snowboard snowboard2 = makeSnowboard2();

		// Different make but same model, should only return model from one of the makes
		assertThat(snowboard1.getMake()).isNotEqualTo(snowboard2.getMake());


		// Set both to null, Burton snowboard make should model subattribute present and noattr value count of 1
		snowboard1.setModel(null);
		snowboard2.setModel(null);

		final StringInCriterium makeCriterium = new StringInCriterium(
				makeAttribute,
				Arrays.asList(new InCriteriumValue<String>("Burton", null)),
				false);
		
		checkSnowboard(snowboard1, snowboard2, (userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ISearchCursor cursor = searchDAO.search(Arrays.asList(Snowboard.class), Arrays.asList(makeCriterium), null, facetedAttributes);

			final List<String> itemIds = cursor.getItemIDs(0, Integer.MAX_VALUE);
			
			assertThat(snowboard1.getMake()).isEqualTo("Burton");

			assertThat(itemIds.size()).isEqualTo(1);
			assertThat(itemIds.get(0)).isEqualTo(itemId1);
			
			assertThat(cursor.getFacets().getTypes().size()).isEqualTo(1);
			final TypeFacets typeFacets = cursor.getFacets().getTypes().get(0);
			
			assertThat(typeFacets.getType()).isEqualTo(Snowboard.class);
			assertThat(typeFacets.getAttributes().size()).isEqualTo(1);
			assertThat(typeFacets.getAttributes().get(0).getAttribute().getName()).isEqualTo("make");
			assertThat(typeFacets.getAttributes().get(0).getNoAttributeValueCount()).isEqualTo(0);
			
			final IndexSingleValueFacetedAttributeResult makeFacet = (IndexSingleValueFacetedAttributeResult)typeFacets.getAttributes().get(0);
			
			assertThat(makeFacet.getValues().size()).isEqualTo(1);

			final IndexSingleValueFacet burtonValue = makeFacet.getValues().get(0);
			
			assertThat(burtonValue.getValue()).isEqualTo("Burton");
			assertThat(burtonValue.getMatchCount()).isEqualTo(1);
			
			assertThat(burtonValue.getSubFacets().size()).isEqualTo(1);
			
			final IndexFacetedAttributeResult subResult = burtonValue.getSubFacets().get(0);
			assertThat(subResult.getAttribute().getName()).isEqualTo("model");
			assertThat(subResult.getNoAttributeValueCount()).isEqualTo(1);
			
			final IndexSingleValueFacetedAttributeResult modelFacet = (IndexSingleValueFacetedAttributeResult)subResult;

			assertThat(modelFacet.getValues().size()).isEqualTo(0);
		});
	}

	public void testSearchWithOnlyNoAttributeValueCriteriaForSubAttribute() throws Exception {
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

		final ItemAttribute makeAttribute = snowboardAttributes.getByName("make");
		final ItemAttribute modelAttribute = snowboardAttributes.getByName("model");

		final Set<ItemAttribute> facetedAttributes = new HashSet<>(Arrays.asList(makeAttribute, modelAttribute));

		final Snowboard snowboard1 = makeSnowboard1();
		final Snowboard snowboard2 = makeSnowboard2();

		// Different make but same model, should only return model from one of the makes
		assertThat(snowboard1.getMake()).isNotEqualTo(snowboard2.getMake());

		assertThat(snowboard1.getMake()).isEqualTo("Burton");
		snowboard1.setModel(null);
		
		final NoValueCriterium modelSubCriterium = new NoValueCriterium(modelAttribute);

		final StringInCriterium makeCriterium = new StringInCriterium(
				makeAttribute,
				Arrays.asList(new InCriteriumValue<String>("Burton", Arrays.asList(modelSubCriterium))),
				false);
		

		checkSnowboard(snowboard1, snowboard2, (userId, itemDAO, searchDAO, itemId1, itemId2) -> {
			final ISearchCursor cursor = searchDAO.search(Arrays.asList(Snowboard.class), Arrays.asList(makeCriterium), null, facetedAttributes);

			final List<String> itemIds = cursor.getItemIDs(0, Integer.MAX_VALUE);

			assertThat(itemIds.size()).isEqualTo(1);
			assertThat(itemIds.get(0)).isEqualTo(itemId1);
			
			assertThat(cursor.getFacets().getTypes().size()).isEqualTo(1);
			final TypeFacets typeFacets = cursor.getFacets().getTypes().get(0);
			
			assertThat(typeFacets.getType()).isEqualTo(Snowboard.class);
			assertThat(typeFacets.getAttributes().size()).isEqualTo(1);
			assertThat(typeFacets.getAttributes().get(0).getAttribute().getName()).isEqualTo("make");
			assertThat(typeFacets.getAttributes().get(0).getNoAttributeValueCount()).isEqualTo(0);

			final IndexSingleValueFacetedAttributeResult makeFacet = (IndexSingleValueFacetedAttributeResult)typeFacets.getAttributes().get(0);
			
			assertThat(makeFacet.getValues().size()).isEqualTo(1);

			final IndexSingleValueFacet burtonValue = makeFacet.getValues().get(0);
			
			assertThat(burtonValue.getSubFacets().size()).isEqualTo(1);
			
			final IndexFacetedAttributeResult subResult = burtonValue.getSubFacets().get(0);
			assertThat(subResult.getAttribute().getName()).isEqualTo("model");
			assertThat(subResult.getNoAttributeValueCount()).isEqualTo(1);

			final IndexSingleValueFacetedAttributeResult modelFacet = (IndexSingleValueFacetedAttributeResult)subResult;

			assertThat(modelFacet.getValues().size()).isEqualTo(0);
		});
	}
	
	public void testSortOrder1() throws Exception {
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

		final ItemAttribute makeAttribute = snowboardAttributes.getByName("make");

		final Snowboard snowboard1 = makeSnowboard1();
		final Snowboard snowboard2 = makeSnowboard2();
		
		snowboard1.setMake("Xyz");
		snowboard2.setMake("Abc");
		
		checkSnowboard(snowboard1, snowboard2, (userId, itemDAO, searchDAO, itemId1, itemId2) -> {

			final ISearchCursor cursor = searchDAO.search(Arrays.asList(Snowboard.class), null, Arrays.asList(makeAttribute), null);
			
			final List<String> itemIds = cursor.getAllItemIDs();
			
			assertThat(itemIds.size()).isEqualTo(2);
			assertThat(itemIds.get(0)).isEqualTo(itemId2);
			assertThat(itemIds.get(1)).isEqualTo(itemId1);

			final List<SearchItem> items = cursor.getAllItemIDsAndTitles();
			assertThat(items.size()).isEqualTo(2);
			assertThat(items.get(0).getItemId()).isEqualTo(itemId2);
			assertThat(items.get(1).getItemId()).isEqualTo(itemId1);
		});
	}

	public void testSortOrder2() throws Exception {
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

		final ItemAttribute makeAttribute = snowboardAttributes.getByName("make");
		final ItemAttribute modelAttribute = snowboardAttributes.getByName("model");

		final Snowboard snowboard1 = makeSnowboard1();
		final Snowboard snowboard2 = makeSnowboard2();
		
		snowboard1.setMake("SameMake");
		snowboard2.setMake("SameMake");

		snowboard1.setModel("Xyz");
		snowboard2.setModel("Abc");
		
		checkSnowboard(snowboard1, snowboard2, (userId, itemDAO, searchDAO, itemId1, itemId2) -> {

			final ISearchCursor cursor = searchDAO.search(Arrays.asList(Snowboard.class), null,
					Arrays.asList(makeAttribute, modelAttribute), null);
			
			final List<String> itemIds = cursor.getAllItemIDs();
			
			assertThat(itemIds.size()).isEqualTo(2);
			assertThat(itemIds.get(0)).isEqualTo(itemId2);
			assertThat(itemIds.get(1)).isEqualTo(itemId1);

			final List<SearchItem> items = cursor.getAllItemIDsAndTitles();
			assertThat(items.size()).isEqualTo(2);
			assertThat(items.get(0).getItemId()).isEqualTo(itemId2);
			assertThat(items.get(1).getItemId()).isEqualTo(itemId1);
		});
	}

	@FunctionalInterface
	public interface CheckSnowboard {
		void check(String userId, IItemDAO itemDAO,  ISearchDAO searchDAO, String itemId1, String itemId2) throws SearchException, Exception;
	}
	
	private void checkSnowboard(CheckSnowboard check) throws Exception {
		final Snowboard snowboard1 = makeSnowboard1();
		final Snowboard snowboard2 = makeSnowboard2();
		
		checkSnowboard(snowboard1, snowboard2, check);
	}

	private void checkSnowboard(Snowboard snowboard1, Snowboard snowboard2, CheckSnowboard check) throws Exception {

		final String  userId = "theUser";
		
		String itemId1 = null;
		String itemId2 = null;
		
		final IItemDAO itemDAO = getItemDAO();
	
		try {
			itemId1 = itemDAO.addItem(userId, snowboard1);
			itemId2 = itemDAO.addItem(userId, snowboard2);
			
			try (ISearchDAO searchDAO = getSearchDAO()) {
				 check.check(userId, itemDAO, searchDAO, itemId1, itemId2);
			}
		}
		finally {
			if (itemId1 != null) {
				 itemDAO.deleteItem(userId, itemId1, Snowboard.class);
			}

			if (itemId2 != null) {
				itemDAO.deleteItem(userId, itemId2, Snowboard.class);
			}
			
			itemDAO.close();
		}
	}
}
