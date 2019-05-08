package com.test.salesportal.rest.search.all.cache;

import com.test.salesportal.common.CollectionUtil;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.model.items.sports.Snowboard;
import com.test.salesportal.model.items.sports.SnowboardProfile;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeRangeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedAttributeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetedTypeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchFacetsResult;
import com.test.salesportal.rest.search.model.facetresult.SearchRangeFacetedAttributeResult;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacet;
import com.test.salesportal.rest.search.model.facetresult.SearchSingleValueFacetedAttributeResult;

import static com.test.salesportal.rest.search.all.cache.CacheTestUtil.decimal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import junit.framework.TestCase;

public class SearchFacetsResultUtilTest extends TestCase {
	
	private static final List<TypeInfo> ALL_TYPES = ItemTypes.getAllTypeInfosList();
	
	public void testAddAndDeleteItems() {
	
		final SearchFacetsResult result = new SearchFacetsResult();
		
		final Snowboard snowboard = new Snowboard();
		
		final TypeInfo snowboardType = ItemTypes.getTypeInfo(Snowboard.class);
		
		final int typesCount = ItemTypes.getAllTypesSet().size();

		/*
		System.out.println("Types : " + ItemTypes.getAllTypesSet().stream()
				.map(Class::getSimpleName)
				.collect(Collectors.toList()));
		*/
		
		snowboard.setProfile(SnowboardProfile.FLAT);
		snowboard.setMake("Burton");

		snowboard.setLength(decimal(165));
		
		SearchFacetsResultUtil.addItem(result, snowboard, snowboardType, ALL_TYPES);
		assertThat(result.getTypes().size()).isEqualTo(typesCount);
		
		final SearchFacetedTypeResult typeResult = CollectionUtil.find(
				result.getTypes(),
				type -> type.getType().equals(snowboardType.getTypeName()));
		
		assertThat(typeResult).isNotNull();
		assertThat(typeResult.getType()).isEqualTo(snowboardType.getTypeName());
		
		final long numFacetAttributes = snowboardType.getAttributes().asSet().stream()
				.filter(ItemAttribute::isFaceted)
				.count();
		
		assertThat((long)typeResult.getAttributes().size()).isEqualTo(numFacetAttributes);
		
		SearchSingleValueFacetedAttributeResult profileAttribute = findAttribute(typeResult.getAttributes(), "profile");
		assertThat(profileAttribute.getNoAttributeValueCount()).isNull();
		assertThat(profileAttribute.getValues().size()).isEqualTo(1);
		checkAttributeValue(profileAttribute.getValues().get(0), 1, SnowboardProfile.FLAT);

		SearchSingleValueFacetedAttributeResult makeAttribute = findAttribute(typeResult.getAttributes(), "make");
		assertThat(makeAttribute.getValues().size()).isEqualTo(1);
		checkAttributeValue(makeAttribute.getValues().get(0), 1, "Burton");
		
		// model is subattribute of make
		assertThat(makeAttribute.getNoAttributeValueCount()).isNull();
		assertThat(makeAttribute.getValues().get(0).getSubAttributes().size()).isEqualTo(1);
		assertThat(makeAttribute.getValues().get(0).getSubAttributes().get(0).getNoAttributeValueCount()).isEqualTo(1);
		
		SearchSingleValueFacetedAttributeResult productionYearAttribute = findAttribute(typeResult.getAttributes(), "productionYear");
		assertThat(productionYearAttribute.getNoAttributeValueCount()).isEqualTo(1);
		assertThat(productionYearAttribute.getValues()).isNull();;
		
		SearchRangeFacetedAttributeResult lengthAttribute = findAttribute(typeResult.getAttributes(), "length");
		
		assertThat(lengthAttribute).isNotNull();
		assertThat(lengthAttribute.getRanges().size()).isEqualTo(4);
		
		checkAttributeValue(lengthAttribute.getRanges().get(0), 0, null, decimal(150));
		checkAttributeValue(lengthAttribute.getRanges().get(1), 0, decimal(150), decimal(160));
		checkAttributeValue(lengthAttribute.getRanges().get(2), 1, decimal(160), decimal(170));
		checkAttributeValue(lengthAttribute.getRanges().get(3), 0, decimal(170), null);
		
		// Add a snowboard with model
		final Snowboard anotherSnowboard = new Snowboard();
		
		anotherSnowboard.setProfile(SnowboardProfile.FLAT);
		anotherSnowboard.setMake("Jones");
		anotherSnowboard.setModel("SomeModel");
		anotherSnowboard.setProductionYear(2015);

		anotherSnowboard.setLength(decimal(170));
	
		SearchFacetsResultUtil.addItem(result, anotherSnowboard, snowboardType, ALL_TYPES);

		assertThat(result.getTypes().size()).isEqualTo(typesCount);
		
		assertThat(typeResult).isNotNull();
		assertThat(typeResult.getType()).isEqualTo(snowboardType.getTypeName());
		
		assertThat((long)typeResult.getAttributes().size()).isEqualTo(numFacetAttributes);
		
		assertThat(profileAttribute.getNoAttributeValueCount()).isNull();
		assertThat(profileAttribute.getValues().size()).isEqualTo(1);
		checkAttributeValue(profileAttribute.getValues().get(0), 2, SnowboardProfile.FLAT);

		assertThat(makeAttribute.getValues().size()).isEqualTo(2);
		checkAttributeValue(makeAttribute.getValues().get(0), 1, "Burton");
		checkAttributeValue(makeAttribute.getValues().get(1), 1, "Jones");
		
		// model is subattribute of make

		assertThat(makeAttribute.getValues().get(0).getSubAttributes().size()).isEqualTo(1);
		final SearchSingleValueFacetedAttributeResult burtonModelSubAttribute
			= (SearchSingleValueFacetedAttributeResult)makeAttribute.getValues().get(0).getSubAttributes().get(0);

		assertThat(burtonModelSubAttribute.getId()).isEqualTo("model");
		assertThat(burtonModelSubAttribute.getName()).isEqualTo("Model");
		assertThat(burtonModelSubAttribute.getValues()).isNull();
		assertThat(burtonModelSubAttribute.getNoAttributeValueCount()).isEqualTo(1);
		
		assertThat(makeAttribute.getValues().get(1).getSubAttributes().size()).isEqualTo(1);
		SearchSingleValueFacetedAttributeResult jonesModelSubAttribute
			= (SearchSingleValueFacetedAttributeResult)makeAttribute.getValues().get(1).getSubAttributes().get(0);

		assertThat(jonesModelSubAttribute.getId()).isEqualTo("model");
		assertThat(jonesModelSubAttribute.getName()).isEqualTo("Model");
		checkAttributeValue(jonesModelSubAttribute.getValues().get(0), 1, "SomeModel");
		assertThat(jonesModelSubAttribute.getNoAttributeValueCount()).isNull();
		
		assertThat(productionYearAttribute.getNoAttributeValueCount()).isEqualTo(1);
		assertThat(productionYearAttribute.getValues().size()).isEqualTo(1);
		checkAttributeValue(productionYearAttribute.getValues().get(0), 1, 2015);
		
		assertThat(lengthAttribute).isNotNull();
		assertThat(lengthAttribute.getRanges().size()).isEqualTo(4);
		
		checkAttributeValue(lengthAttribute.getRanges().get(0), 0, null, decimal(150));
		checkAttributeValue(lengthAttribute.getRanges().get(1), 0, decimal(150), decimal(160));
		checkAttributeValue(lengthAttribute.getRanges().get(2), 1, decimal(160), decimal(170));
		checkAttributeValue(lengthAttribute.getRanges().get(3), 1, decimal(170), null);
		
		// Delete an item
		SearchFacetsResultUtil.deleteItem(result, snowboard, snowboardType);

		assertThat(result.getTypes().size()).isEqualTo(typesCount);
		
		assertThat(typeResult).isNotNull();
		assertThat(typeResult.getType()).isEqualTo(snowboardType.getTypeName());
		
		assertThat((long)typeResult.getAttributes().size()).isEqualTo(numFacetAttributes);
		
		assertThat(profileAttribute.getNoAttributeValueCount()).isNull();
		assertThat(profileAttribute.getValues().size()).isEqualTo(1);
		checkAttributeValue(profileAttribute.getValues().get(0), 1, SnowboardProfile.FLAT);

		assertThat(makeAttribute.getValues().size()).isEqualTo(1);
		checkAttributeValue(makeAttribute.getValues().get(0), 1, "Jones");
		
		assertThat(makeAttribute.getValues().get(0).getSubAttributes().size()).isEqualTo(1);
		jonesModelSubAttribute
			= (SearchSingleValueFacetedAttributeResult)makeAttribute.getValues().get(0).getSubAttributes().get(0);

		assertThat(jonesModelSubAttribute.getId()).isEqualTo("model");
		assertThat(jonesModelSubAttribute.getName()).isEqualTo("Model");
		checkAttributeValue(jonesModelSubAttribute.getValues().get(0), 1, "SomeModel");
		assertThat(jonesModelSubAttribute.getNoAttributeValueCount()).isNull();
		
		assertThat(productionYearAttribute.getNoAttributeValueCount()).isNull();
		assertThat(productionYearAttribute.getValues().size()).isEqualTo(1);
		checkAttributeValue(productionYearAttribute.getValues().get(0), 1, 2015);

		assertThat(lengthAttribute).isNotNull();
		assertThat(lengthAttribute.getRanges().size()).isEqualTo(4);
		
		checkAttributeValue(lengthAttribute.getRanges().get(0), 0, null, decimal(150));
		checkAttributeValue(lengthAttribute.getRanges().get(1), 0, decimal(150), decimal(160));
		checkAttributeValue(lengthAttribute.getRanges().get(2), 0, decimal(160), decimal(170));
		checkAttributeValue(lengthAttribute.getRanges().get(3), 1, decimal(170), null);

		// Delete last item
		SearchFacetsResultUtil.deleteItem(result, anotherSnowboard, snowboardType);

		assertThat(result.getTypes().size()).isEqualTo(typesCount);
		
		assertThat(typeResult).isNotNull();
		assertThat(typeResult.getType()).isEqualTo(snowboardType.getTypeName());
		
		assertThat((long)typeResult.getAttributes().size()).isEqualTo(numFacetAttributes);
		
		assertThat(profileAttribute.getNoAttributeValueCount()).isNull();
		assertThat(profileAttribute.getValues()).isNull();

		assertThat(makeAttribute.getNoAttributeValueCount()).isNull();
		assertThat(makeAttribute.getValues()).isNull();
		
		assertThat(productionYearAttribute.getNoAttributeValueCount()).isNull();
		assertThat(productionYearAttribute.getValues()).isNull();

		assertThat(lengthAttribute.getRanges().size()).isEqualTo(4);
		
		checkAttributeValue(lengthAttribute.getRanges().get(0), 0, null, decimal(150));
		checkAttributeValue(lengthAttribute.getRanges().get(1), 0, decimal(150), decimal(160));
		checkAttributeValue(lengthAttribute.getRanges().get(2), 0, decimal(160), decimal(170));
		checkAttributeValue(lengthAttribute.getRanges().get(3), 0, decimal(170), null);

		// Should still have all types in list
		assertThat(result.getTypes().size()).isEqualTo(typesCount);

		
		// Add another to check that works to add after deletions
		
		final Snowboard yetAnotherSnowboard = new Snowboard();
		
		yetAnotherSnowboard.setProfile(SnowboardProfile.CAMBER);
		yetAnotherSnowboard.setMake("SomeOtherMake");
		yetAnotherSnowboard.setModel("SomeOtherModel");
		yetAnotherSnowboard.setProductionYear(2005);

		yetAnotherSnowboard.setLength(decimal(150));
		
		SearchFacetsResultUtil.addItem(result, yetAnotherSnowboard, snowboardType, ALL_TYPES);

		assertThat(result.getTypes().size()).isEqualTo(typesCount);
		
		assertThat(typeResult).isNotNull();
		assertThat(typeResult.getType()).isEqualTo(snowboardType.getTypeName());
		
		assertThat((long)typeResult.getAttributes().size()).isEqualTo(numFacetAttributes);
		
		assertThat(profileAttribute.getNoAttributeValueCount()).isNull();
		assertThat(profileAttribute.getValues().size()).isEqualTo(1);
		checkAttributeValue(profileAttribute.getValues().get(0), 1, SnowboardProfile.CAMBER);

		assertThat(makeAttribute.getNoAttributeValueCount()).isNull();
		assertThat(makeAttribute.getValues().size()).isEqualTo(1);
		checkAttributeValue(makeAttribute.getValues().get(0), 1, "SomeOtherMake");
		
		assertThat(makeAttribute.getValues().get(0).getSubAttributes().size()).isEqualTo(1);
		jonesModelSubAttribute
			= (SearchSingleValueFacetedAttributeResult)makeAttribute.getValues().get(0).getSubAttributes().get(0);

		assertThat(jonesModelSubAttribute.getId()).isEqualTo("model");
		assertThat(jonesModelSubAttribute.getName()).isEqualTo("Model");
		checkAttributeValue(jonesModelSubAttribute.getValues().get(0), 1, "SomeOtherModel");
		assertThat(jonesModelSubAttribute.getNoAttributeValueCount()).isNull();
		
		assertThat(productionYearAttribute.getNoAttributeValueCount()).isNull();
		assertThat(productionYearAttribute.getValues().size()).isEqualTo(1);
		checkAttributeValue(productionYearAttribute.getValues().get(0), 1, 2005);

		assertThat(lengthAttribute).isNotNull();
		assertThat(lengthAttribute.getRanges().size()).isEqualTo(4);
		
		checkAttributeValue(lengthAttribute.getRanges().get(0), 0, null, decimal(150));
		checkAttributeValue(lengthAttribute.getRanges().get(1), 1, decimal(150), decimal(160));
		checkAttributeValue(lengthAttribute.getRanges().get(2), 0, decimal(160), decimal(170));
		checkAttributeValue(lengthAttribute.getRanges().get(3), 0, decimal(170), null);
	}
	
	private void checkAttributeValue(SearchSingleValueFacet valueFacet, int matchCount, Object value) {
		assertThat(valueFacet.getMatchCount()).isEqualTo(matchCount);
		assertThat(valueFacet.getValue()).isEqualTo(value);
		assertThat(valueFacet.getDisplayValue()).isNull();
	}

	private void checkAttributeValue(SearchFacetedAttributeRangeResult<?> rangeFacet, int matchCount, Object lower, Object upper) {
		assertThat(rangeFacet.getMatchCount()).isEqualTo(matchCount);
		
		if (lower == null) {
			assertThat(rangeFacet.getLower()).isNull();
		}
		else {
			
			assertThat(rangeFacet.getLower()).isNotNull();
			
			@SuppressWarnings("unchecked")
			final Comparable<Object> lowerComparable = (Comparable<Object>)lower;
			@SuppressWarnings("unchecked")
			final Comparable<Object> rangeLowerComparable = (Comparable<Object>)rangeFacet.getLower();
			
			assertThat(rangeLowerComparable.compareTo(lowerComparable)).isEqualTo(0);
		}
		
		if (upper == null) {
			assertThat(rangeFacet.getUpper()).isNull();
		}
		else {
			
			assertThat(rangeFacet.getUpper()).isNotNull();
			
			@SuppressWarnings("unchecked")
			final Comparable<Object> upperComparable = (Comparable<Object>)upper;
			@SuppressWarnings("unchecked")
			final Comparable<Object> rangeUpperComparable = (Comparable<Object>)rangeFacet.getUpper();
			
			assertThat(rangeUpperComparable.compareTo(upperComparable)).isEqualTo(0);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends SearchFacetedAttributeResult> T findAttribute(List<SearchFacetedAttributeResult> attributes, String attrId) {
		return (T)CollectionUtil.find(attributes, attribute -> attribute.getId().equals(attrId));
	}
}
