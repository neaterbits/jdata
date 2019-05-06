package com.test.salesportal.rest.search.all.cache;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.test.salesportal.common.StringUtil;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.attributes.AttributeType;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.TypeInfo;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;
import com.test.salesportal.rest.search.model.criteria.SearchCriteriumValue;
import com.test.salesportal.rest.search.model.criteria.SearchRange;


class SearchKeyMatchUtil {

	static boolean matchesSearchKey(SearchKey searchKey, Item item) {
		
		final TypeInfo itemTypeInfo = ItemTypes.getTypeInfo(item);
		
		return matchesSearchCriteria(searchKey.getCriteria(), item, itemTypeInfo)
				&& matchesFreeText(searchKey.getFreeText(), item, itemTypeInfo);
				
		
	}
	
	private static boolean matchesSearchCriteria(List<SearchCriterium> criteria, Item item, TypeInfo itemTypeInfo) {
	
		boolean matches = false;
		
		for (SearchCriterium criterium : criteria) {
			if (matchesSearchCriterium(criterium, item, itemTypeInfo)) {
				matches = true;
				break;
			}
		}
		
		return matches;
	}
	
	private static boolean matchesSearchCriterium(SearchCriterium criterium, Item item, TypeInfo typeInfo) {
		
		final boolean matches;
		
		if (!criterium.getType().equals(typeInfo.getTypeName())) {
			matches = false;
		}
		else {
			final ItemAttribute attribute = typeInfo.getAttributes().getByName(criterium.getAttribute());
			
			if (attribute == null) {
				throw new IllegalStateException();
			}
			
			final Object attributeValueObject = attribute.getObjectValue(item);
			
			if (attributeValueObject == null) {
				
				matches = criterium.getOtherSelected() != null && criterium.getOtherSelected();
			}
			else {

				if (attribute.isSingleValue()) {
					
					boolean matchFound = false;
					
					for (SearchCriteriumValue searchCriteriumValue : criterium.getValues()) {
						
						if (matchesSearchCriteriumValue(searchCriteriumValue, attributeValueObject, item, typeInfo)) {
							matchFound = true;
							break;
						}
					}
					
					matches = matchFound;
					
				}
				else if (attribute.isRange()) {

					boolean matchFound = false;
					
					for (SearchRange searchRange : criterium.getRanges()) {
						if (matchesSearchRange(attribute, searchRange, attributeValueObject)) {
							matchFound = true;
							break;
						}
					}
					
					matches = matchFound;
				}
				else {
					throw new UnsupportedOperationException();
				}
			}
		}
		
		return matches;
	}
	
	private static boolean matchesSearchCriteriumValue(SearchCriteriumValue searchCriteriumValue, Object valueObject, Item item, TypeInfo itemTypeInfo) {
		
		boolean matches = searchCriteriumValue.getValue().equals(valueObject);
		
		if (matches && searchCriteriumValue.getSubCriteria() != null) {
			matches = matchesSearchCriteria(Arrays.asList(searchCriteriumValue.getSubCriteria()), item, itemTypeInfo);
		}
		
		return matches;
	}
	
	private static boolean matchesSearchRange(ItemAttribute attribute, SearchRange searchRange, Object valueObject) {
		
		final boolean matches;
		
		if (valueObject == null) {
			matches = false;
		}
		else {
		
			final Comparator<Object> comparator;
			
			switch (attribute.getAttributeType()) {
			
			case INTEGER:
				comparator = (obj1, obj2) -> Integer.compare((Integer)obj1, (Integer)obj2);
				break;
				
			case LONG:
				comparator = (obj1, obj2) -> Long.compare((Long)obj1, (Long)obj2);
				break;
				
			case DECIMAL:
				comparator = (obj1, obj2) -> ((BigDecimal)obj1).compareTo((BigDecimal)obj2);
				break;
				
			case DATE:
				comparator = (obj1, obj2) -> ((Date)obj1).compareTo((Date)obj2);
				break;
				
			default:
				throw new UnsupportedOperationException();
			
			}
			
			if (searchRange.getLower() == null && searchRange.getUpper() == null) {
				throw new IllegalArgumentException();
			}
			else if (searchRange.getLower() == null) {
				
				final int comparison = comparator.compare(valueObject, searchRange.getUpper());
				
				matches = searchRange.includeUpper()
						? comparison <= 0
						: comparison < 0;
			}
			else if (searchRange.getUpper() == null) {

				final int comparison = comparator.compare(valueObject, searchRange.getLower());
				
				matches = searchRange.includeLower()
						? comparison >= 0
						: comparison > 0;
			}
			else {
				final int lowerComparison = comparator.compare(valueObject, searchRange.getLower());
				final int upperComparison = comparator.compare(valueObject, searchRange.getUpper());
				
				matches =
						   (searchRange.includeLower() ? lowerComparison >= 0 : lowerComparison > 0)
						&& (searchRange.includeUpper() ? upperComparison <= 0 : upperComparison < 0);
			}
		}
		
		return matches;
	}
	
	private static boolean matchesFreeText(String freeText, Item item, TypeInfo itemTypeInfo) {
		
		boolean matches;
		
		if (freeText == null) {
			matches = true;
		}
		else {
			final String trimmed = freeText.trim();
			
			if (trimmed.isEmpty()) {
				matches = true;
			}
			else {
				boolean matchFound = false;
				
				for (ItemAttribute itemAttribute : itemTypeInfo.getAttributes().asSet()) {
					
					if (itemAttribute.isFreetext()) {
						if (itemAttribute.getAttributeType() != AttributeType.STRING) {
							throw new IllegalStateException();
						}
						
						final String attributeText = (String)itemAttribute.getObjectValue(item);

						if (StringUtil.containsWholeWord(attributeText, trimmed, false)) {
							matchFound = true;
							break;
						}
					}
				}
				
				matches = matchFound;
			}
		}
		
		return matches;
	}
}
