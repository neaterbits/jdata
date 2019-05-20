package com.test.salesportal.rest.search.all.cache;

import java.util.ArrayList;
import java.util.List;

import com.test.salesportal.model.items.Item;
import com.test.salesportal.model.items.ItemAttribute;
import com.test.salesportal.model.items.SortAttribute;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.model.items.sales.SalesItemTypes;
import com.test.salesportal.model.items.sports.DownhillSki;
import com.test.salesportal.model.items.sports.Snowboard;
import com.test.salesportal.model.items.sports.SnowboardProfile;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.model.criteria.SearchCriteriumValue;
import com.test.salesportal.rest.search.model.criteria.SearchRange;

import static org.assertj.core.api.Assertions.assertThat;

import junit.framework.TestCase;

public class SearchKeyTest extends TestCase {

	private static final ItemTypes ITEM_TYPES = SalesItemTypes.INSTANCE;
	
	public void testEqualsHashCodeTypes() {

		final List<Class<? extends Item>> types = new ArrayList<>();
		
		types.add(Snowboard.class);

		final SearchKey searchKey1 = new SearchKey(types, null, null, null, null);
		
		types.add(DownhillSki.class);
		
		// Should not update list in key
		assertThat(searchKey1.getTypes().size()).isEqualTo(1);
		assertThat(searchKey1.getTypes().get(0)).isEqualTo(Snowboard.class);
		
		final SearchKey searchKey2 = new SearchKey(types, null, null, null, null);
		
		assertThat(searchKey1.equals(searchKey2)).isFalse();
		assertThat(searchKey1.hashCode() == searchKey2.hashCode()).isFalse();

		final SearchKey searchKey3 = new SearchKey(types, null, null, null, null);
		assertThat(searchKey3.equals(searchKey2)).isTrue();
		assertThat(searchKey3.hashCode() == searchKey2.hashCode()).isTrue();
	}

	public void testEqualsHashCodeFreetext() {

		final SearchKey searchKey1 = new SearchKey(null, "freeText", null, null, null);
		
		// Should not update list in key
		final SearchKey searchKey2 = new SearchKey(null, "anotherFreetext", null, null, null);
		
		assertThat(searchKey1.equals(searchKey2)).isFalse();
		assertThat(searchKey1.hashCode() == searchKey2.hashCode()).isFalse();

		final SearchKey searchKey3 = new SearchKey(null, "freeText", null, null, null);
		assertThat(searchKey1.equals(searchKey3)).isTrue();
		assertThat(searchKey1.hashCode() == searchKey3.hashCode()).isTrue();
	}

	public void testEqualsHashCodeSearchCriteria() {

		final TypeInfo snowboardType = ITEM_TYPES.getTypeInfo(Snowboard.class);
		final TypeInfo downhillSkiType = ITEM_TYPES.getTypeInfo(DownhillSki.class);
		
		final ItemAttribute profileAttribute = snowboardType.getAttributes().getByName("profile");
		assertThat(profileAttribute).isNotNull();
		
		final ItemAttribute downhillSkiWidthAttribute = downhillSkiType.getAttributes().getByName("width");
		assertThat(downhillSkiWidthAttribute).isNotNull();
				
		
		final SearchCriterium snowboardProfileSearchCriterium = new SearchCriterium(
				snowboardType.getTypeName(),
				profileAttribute.getName(),
				new SearchCriteriumValue [] { new SearchCriteriumValue(SnowboardProfile.FLAT) },
				null);
		
		
		final SearchCriterium downhillSkiWidthCriterium = new SearchCriterium(
				downhillSkiType.getTypeName(),
				downhillSkiWidthAttribute.getName(),
				new SearchRange [] {
						new SearchRange(null, true, 150, false),
						new SearchRange(150, true, 160, false),
						new SearchRange(160, true, null, false)
				});
		
		final SearchCriterium [] searchCriteria1 = new SearchCriterium [] {
				snowboardProfileSearchCriterium
		};
		
		final SearchKey searchKey1 = new SearchKey(null, null, searchCriteria1, null, null);

		final SearchCriterium [] searchCriteria2 = new SearchCriterium [] {
				snowboardProfileSearchCriterium,
				downhillSkiWidthCriterium
		};

		
		final SearchKey searchKey2 = new SearchKey(null, null, searchCriteria2, null, null);
		
		assertThat(searchKey1.equals(searchKey2)).isFalse();
		assertThat(searchKey1.hashCode() == searchKey2.hashCode()).isFalse();

		final SearchCriterium [] searchCriteria3 = new SearchCriterium [] {
				snowboardProfileSearchCriterium,
				downhillSkiWidthCriterium
		};

		final SearchKey searchKey3 = new SearchKey(null, null, searchCriteria3, null, null);
		assertThat(searchKey3.equals(searchKey2)).isTrue();
		assertThat(searchKey3.hashCode() == searchKey2.hashCode()).isTrue();
	}

	public void testEqualsHashCodeSortAttribute() {

		final List<SortAttribute> sortAttributes = new ArrayList<>();

		final TypeInfo snowboardType = ITEM_TYPES.getTypeInfo(Snowboard.class);
		
		final SortAttribute titleAttribute = snowboardType.getAttributes().getByName("title").makeSortAttribute();
		assertThat(titleAttribute).isNotNull();
				
		final SortAttribute makeAttribute = snowboardType.getAttributes().getByName("make").makeSortAttribute();
		assertThat(makeAttribute).isNotNull();
		
		sortAttributes.add(titleAttribute);

		final SearchKey searchKey1 = new SearchKey(null, null, null, sortAttributes, null);
		
		sortAttributes.add(makeAttribute);
		
		// Should not update list in key
		assertThat(searchKey1.getSortAttributes().size()).isEqualTo(1);
		assertThat(searchKey1.getSortAttributes().get(0)).isEqualTo(titleAttribute);
		
		final SearchKey searchKey2 = new SearchKey(null, null, null, sortAttributes, null);
		
		assertThat(searchKey1.equals(searchKey2)).isFalse();
		assertThat(searchKey1.hashCode() == searchKey2.hashCode()).isFalse();

		final SearchKey searchKey3 = new SearchKey(null, null, null, sortAttributes, null);
		assertThat(searchKey3.equals(searchKey2)).isTrue();
		assertThat(searchKey3.hashCode() == searchKey2.hashCode()).isTrue();
	}

	public void testEqualsHashCodeFieldAttribute() {

		final List<ItemAttribute> fieldAttributes = new ArrayList<>();

		final TypeInfo snowboardType = ITEM_TYPES.getTypeInfo(Snowboard.class);
		
		final ItemAttribute titleAttribute = snowboardType.getAttributes().getByName("title");
		assertThat(titleAttribute).isNotNull();
				
		final ItemAttribute makeAttribute = snowboardType.getAttributes().getByName("make");
		assertThat(makeAttribute).isNotNull();
		
		fieldAttributes.add(titleAttribute);

		final SearchKey searchKey1 = new SearchKey(null, null, null, null, fieldAttributes);
		
		fieldAttributes.add(makeAttribute);
		
		// Should not update list in key
		assertThat(searchKey1.getFieldAttributes().size()).isEqualTo(1);
		assertThat(searchKey1.getFieldAttributes().get(0)).isEqualTo(titleAttribute);
		
		final SearchKey searchKey2 = new SearchKey(null, null, null, null, fieldAttributes);
		
		assertThat(searchKey1.equals(searchKey2)).isFalse();
		assertThat(searchKey1.hashCode() == searchKey2.hashCode()).isFalse();

		final SearchKey searchKey3 = new SearchKey(null, null, null, null, fieldAttributes);
		assertThat(searchKey3.equals(searchKey2)).isTrue();
		assertThat(searchKey3.hashCode() == searchKey2.hashCode()).isTrue();
	}
}

