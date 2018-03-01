package com.test.cv.dao.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ISearchCursor;
import com.test.cv.dao.ISearchDAO;
import com.test.cv.dao.SearchException;
import com.test.cv.model.ItemAttribute;
import com.test.cv.model.attributes.ClassAttributes;
import com.test.cv.model.items.Snowboard;
import com.test.cv.model.items.SnowboardProfile;
import com.test.cv.search.SearchItem;
import com.test.cv.search.criteria.ComparisonOperator;
import com.test.cv.search.criteria.DecimalCriterium;

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
