package com.test.cv.dao.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.SearchException;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.attributes.ClassAttributes;
import com.test.cv.model.items.sports.Snowboard;
import com.test.cv.model.items.sports.SnowboardProfile;
import com.test.cv.search.SearchItem;
import com.test.cv.search.criteria.ComparisonOperator;
import com.test.cv.search.criteria.DecimalCriterium;
import com.test.cv.search.criteria.DecimalInCriterium;
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

	public void testSearchNoCriteria() throws Exception {

		checkSnowboard((searchDAO, itemId1, itemId2) -> {
			final ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
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

		checkSnowboard((searchDAO, itemId1, itemId2) -> {
			final ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
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
		checkSnowboard((searchDAO, itemId1, itemId2) -> {
			final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

			final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");
			 
			assertThat(widthAttribute).isNotNull();
			 
			final DecimalCriterium widthCriteria = new DecimalCriterium(widthAttribute, new BigDecimal("30.4"), ComparisonOperator.GREATER_THAN);
			
			final ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					Arrays.asList(widthCriteria),
					null);
			final List<String> itemIds = search.getItemIDs(0, Integer.MAX_VALUE);
			
			assertThat(itemIds.size()).isEqualTo(1);
			assertThat(itemIds.contains(itemId2)).isTrue();
		});
	}

	public void testSnowboardCriteriaNotEquals() throws Exception {
		checkSnowboard((searchDAO, itemId1, itemId2) -> {
			final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

			final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");
			 
			assertThat(widthAttribute).isNotNull();
			 
			final DecimalCriterium widthCriteria = new DecimalCriterium(widthAttribute, new BigDecimal("32.8"), ComparisonOperator.NOT_EQUALS);
			
			final ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					Arrays.asList(widthCriteria),
					null);
			final List<String> itemIds = search.getItemIDs(0, Integer.MAX_VALUE);
			
			assertThat(itemIds.size()).isEqualTo(1);
			assertThat(itemIds.contains(itemId1)).isTrue();
		});
	}
	
	public void testSnowboardIn() throws Exception {
		checkSnowboard((searchDAO, itemId1, itemId2) -> {
			final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

			final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");
			 
			assertThat(widthAttribute).isNotNull();
			 
			DecimalInCriterium widthCriterium = new DecimalInCriterium(widthAttribute, new BigDecimal [] { new BigDecimal("30.4"), new BigDecimal("32.8") }, false);
			
			ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					Arrays.asList(widthCriterium),
					null);
			
			List<String> itemIds = search.getItemIDs(0, Integer.MAX_VALUE);
			
			assertThat(itemIds.size()).isEqualTo(2);
			assertThat(itemIds.contains(itemId1)).isTrue();
			assertThat(itemIds.contains(itemId2)).isTrue();
			
			// match only one
			widthCriterium = new DecimalInCriterium(widthAttribute, new BigDecimal [] { new BigDecimal("32.4"), new BigDecimal("32.8") }, false);
			
			search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					Arrays.asList(widthCriterium),
					null);
			
			itemIds = search.getItemIDs(0, Integer.MAX_VALUE);
			
			assertThat(itemIds.size()).isEqualTo(1);
			assertThat(itemIds.contains(itemId2)).isTrue();
		});
		
	}
	
	public void testFacetsNoCriteria() throws Exception {
		
		final ClassAttributes snowboardAttributes = ClassAttributes.getFromClass(Snowboard.class);

		final ItemAttribute widthAttribute = snowboardAttributes.getByName("width");
		final ItemAttribute makeAttribute = snowboardAttributes.getByName("make");

		final Set<ItemAttribute> facetedAttributes = new HashSet<>(Arrays.asList(widthAttribute, makeAttribute));
		
		checkSnowboard((searchDAO, itemId1, itemId2) -> {

			final ISearchCursor search = searchDAO.search(
					Arrays.asList(Snowboard.class),
					null,
					facetedAttributes);
			
			final ItemsFacets facets = search.getFacets();
			
			assertThat(facets).isNotNull();
			assertThat(facets.getTypes().size()).isEqualTo(1);

			final TypeFacets typeFacets = facets.getTypes().get(0);
			
			assertThat(typeFacets.getType()).isEqualTo(Snowboard.class);

			// System.out.println("Attributes: " + typeFacets.getAttributes());
			assertThat(typeFacets.getAttributes().size()).isEqualTo(2);

			final IndexSingleValueFacetedAttributeResult makeFacet =
					(IndexSingleValueFacetedAttributeResult) find(
							typeFacets.getAttributes(),
							attribute -> attribute.getAttribute().getName().equals("make"));
			
			assertThat(makeFacet).isNotNull();
			assertThat(makeFacet.getAttribute()).isEqualTo(makeAttribute);
			assertThat(makeFacet.getValues().size()).isEqualTo(2);
			
			assertThat(makeFacet.getValues().get(0).getMatchCount()).isEqualTo(1);
			assertThat(makeFacet.getValues().get(0).getMatchCount()).isEqualTo(1);

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
			
		});
	}
	
	@FunctionalInterface
	public interface CheckSnowboard {
		void check(ISearchDAO searchDAO, String itemId1, String itemId2) throws SearchException;
	}
	
	private void checkSnowboard(CheckSnowboard check) throws Exception {

		final String  userId = "theUser";
		final Snowboard snowboard1 = makeSnowboard1();
		final Snowboard snowboard2 = makeSnowboard2();
		
		final String itemId1;
		final String itemId2;
		
		try (IItemDAO itemDAO = getItemDAO()) {
			 itemId1 = itemDAO.addItem(userId, snowboard1);
			 itemId2 = itemDAO.addItem(userId, snowboard2);
		
			 try (ISearchDAO searchDAO = getSearchDAO()) {
				 
				 check.check(searchDAO, itemId1, itemId2);
				 
			 }

			 itemDAO.deleteItem(userId, itemId1);
			 itemDAO.deleteItem(userId, itemId2);
		}
	}
	
	
}
